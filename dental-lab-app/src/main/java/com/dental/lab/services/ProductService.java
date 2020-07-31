package com.dental.lab.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

/**
 * 
 * @author Luis Fernando Martinez Oritz
 *
 */
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
	public List<ProductCategory> buildCategoryPath(ProductCategory category) {
		List<ProductCategory> categoryPath = new ArrayList<ProductCategory>();
		
		ProductCategory tempCategory = category;
		do {
			categoryPath.add(tempCategory);
			tempCategory = tempCategory.getParent();
		} while(tempCategory != null);
		
		Collections.reverse(categoryPath);
		
		return categoryPath;
	}
	
	@Transactional(readOnly = true)
	public List<ProductCategory> buildProductCategoryPath(Product product) {
		List<ProductCategory> productCategories = categoryService.findByProduct(product);
		productCategories.sort(Comparator.comparingInt(ProductCategory::getDepth));
		return productCategories;
	}
	
	/**
	 * Retrieves {@linkplain ProductPricing} object with the latest (maximum) {@code starting_date}
	 * entry in the database. This tuple corresponds to the current {@linkplain Product}'s price.
	 * Tuples with smaller {@code starting_date} entries correspond with prices that were used
	 * in the past for the corresponding product.
	 *
	 * @param productId The id of the {@linkplain Product} we require the price for.
	 * @return {@linkplain ProductPricing} object with the latest {@code startingDate} field
	 */
	@Transactional(readOnly = true)
	public ProductPricing findCurrentPriceByProductId(Long productId) {
		return productPricingRepo.findCurrentPriceByProductId(productId)
				.orElseThrow(() -> new EntityNotFoundException("ProductPricing entity with id: " + productId + " was not found!"));
	}
	
	/**
	 * Updates the {@code productImage} field in the {@linkplain Product} 
	 * entity with id {@code productId}.
	 * 
	 * @param productId id of the product which {@code productImage} field is going to be updated.
	 * @param imageToUpdate {@code byte[]} object containing the image to be used to update the
	 * 			{@code productImage} field of the {@linkplain Product} with id {@code productId}
	 * @return {@linkplain Product} object with id {@code productId}, and {@code productImage} updated.
	 * @throws EntityNotFoundException
	 */
	@Transactional
	public Product updateProductThumbnailImage(Long productId, byte[] imageToUpdate) 
			throws EntityNotFoundException {
		
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("Product with id: " + productId + " was not found!"));
		
		product.setProductImage(imageToUpdate);
		return productRepo.save(product);
				
	}
	
	@Transactional
	public Product updateProduct(
			Long productId, Product productUpdated, BigDecimal newPrice) throws EntityNotFoundException {
		
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("Product with id: " + productId + " was not found!"));
		
		product.setName(productUpdated.getName());
		product.setDescription(productUpdated.getDescription());
		updateProductPrice(product, newPrice);
		
		return productRepo.save(product);
	}
	
	@Transactional
	public ProductPricing updateProductPrice(Product product, BigDecimal newPrice) {
		/*
		 * Since the ProductPricing with the latest startingDate will not longer
		 * be the current price of the Product with id productId, the endingDate 
		 * field in ProductPricing with the latest startingDate must be updated 
		 * to the current date. This way the new ProductPricing with latest 
		 * startingDate will be the one we are about to add.
		 */
		ProductPricing currentPrice =
				productPricingRepo.findCurrentPriceByProductId(product.getId())
				.orElseThrow(() -> new EntityNotFoundException("ProductPricing for Product with id: " + product.getId() + " was not found!"));
		// TODO: get right date.
		currentPrice.setEndingDate(new Timestamp((new Date()).getTime()));
		productPricingRepo.save(currentPrice);
		
		/* Product price is not actually updated but a new instance of ProductPricing
		 * is created with startingDate equal to the current date and time. Current 
		 * product price is always taken as the ProductPricing with the latest startingDate.
		 */
		ProductPricing newProductPricing = new ProductPricing(newPrice);
		newProductPricing.setProduct(product);
		product.getProductPricings().add(newProductPricing);
		
		return productPricingRepo.save(newProductPricing);
	}

}
