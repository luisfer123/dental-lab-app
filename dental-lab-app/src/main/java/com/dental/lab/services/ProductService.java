package com.dental.lab.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductCategory;
import com.dental.lab.model.entities.ProductPricing;
import com.dental.lab.model.payloads.ProductPayload;
import com.dental.lab.repositories.ProductPricingRepository;
import com.dental.lab.repositories.ProductRepository;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ProductPricingRepository productPricingRepo;
	
	@Autowired
	private ProductCategoryService categoryService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Transactional(readOnly = true)
	public List<Product> findAllProducts() {
		return productRepo.findAll();
	}
	
	@Transactional(readOnly = true)
	public Product findById(Long id) {
		Product product = productRepo.findById(id).orElseThrow(
				() -> new EntityNotFoundException("Entity Product with id: " + id + " does not exists"));
		
		if(product.getProductImage() == null || product.getProductImage().length == 0) {
			try {
				Resource imageResource = resourceLoader.getResource("classpath:static/images/No-photo-product.jpg");
				byte[] image = Files.readAllBytes(Paths.get(imageResource.getURI()));
				product.setProductImage(image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return product;
	}
	
	@Transactional(readOnly = true)
	public ProductPricing findCurrentPrice(Long productId) {
		
		ProductPricing pp = productPricingRepo.findCurrentPriceByProductId(productId)
				.orElseThrow(() -> new EntityNotFoundException());
		
		return pp;
	}
	
	@Transactional(readOnly = true)
	public List<Product> findByCategoryId(Long id) {
		return productRepo.findByCategoryId(id);
	}
	
	@Transactional(readOnly = true)
	public List<ProductPayload> createProductPayload(List<Product> products) {
				
		/*
		 *  Add the default image to the products that do not have photo.
		 */
		byte[] image = null;
		for(Product product: products) {
			if(product.getProductImage() == null || product.getProductImage().length <= 0) {
				if(image == null) {
					try {
						Resource imageResource = resourceLoader.getResource("classpath:static/images/No-photo-product.jpg");
						image = Files.readAllBytes(Paths.get(imageResource.getURI()));
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				product.setProductImage(image);
			}
		}
		
		/*
		 * Create PayloadProduct for sending products to the view. PayloadProduct
		 * object contains a string representation of the productPicture field instead
		 * of a byte[] object.
		 * 
		 */
		List<ProductPayload> productsPayload = products
			.stream()
				.map(product -> {
					ProductPricing currentPrice = findCurrentPrice(product.getId());
					ProductPayload productPayload = new ProductPayload(product, currentPrice.getPrice());
					return productPayload;
				})
				.collect(Collectors.toList());
		
		return productsPayload;
		
	}
	
	@Transactional(readOnly = true)
	public List<ProductCategory> buildCategoryPathWithoutRoot(ProductCategory category) {
		List<ProductCategory> categoryPath = new ArrayList<ProductCategory>();
		
		ProductCategory tempCategory = category;
		while(tempCategory.getParent() != null) {
			categoryPath.add(tempCategory);
			tempCategory = tempCategory.getParent();
		}
		Collections.reverse(categoryPath);
		
		return categoryPath;
	}
	
	@Transactional(readOnly = true)
	public List<ProductCategory> buildProductCategoryPath(Product product) {
		List<ProductCategory> productCategories = categoryService.findByProduct(product);
		productCategories.sort(Comparator.comparingInt(ProductCategory::getDepth));
		return productCategories;
	}

}
