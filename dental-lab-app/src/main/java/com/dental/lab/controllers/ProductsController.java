package com.dental.lab.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductCategory;
import com.dental.lab.model.entities.ProductPricing;
import com.dental.lab.model.payloads.ProductPayload;
import com.dental.lab.services.ProductCategoryService;
import com.dental.lab.services.ProductService;

@Controller
@RequestMapping(path = "/products")
public class ProductsController {
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductCategoryService categoryService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@RequestMapping(path = "/products-list")
	public ModelAndView goProductsList(ModelMap model) {
		
		List<Product> products = productService.findAllProducts();
		
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
					ProductPricing currentPrice = productService.findCurrentPrice(product.getId());
					ProductPayload productPayload = new ProductPayload(product, currentPrice.getPrice());
					return productPayload;
				})
				.collect(Collectors.toList());
		
		model.addAttribute("products", productsPayload);
		
		return new ModelAndView("/products/products-list", model);
	}
	
	@RequestMapping(path = "/{categoryId}/list")
	public ModelAndView goProductsCategory(ModelMap model,
			@PathVariable("categoryId") Long categoryId) {
				
		ProductCategory category = categoryService.findById(categoryId);
		List<Product> products = 
				productService.findByCategoryId(categoryId);
		List<ProductPayload> productsPayload = 
				productService.createProductPayload(products);
		List<ProductCategory> categoryPath = 
				productService.buildCategoryPathWithoutRoot(category);
		
		model.addAttribute("category", category);
		model.addAttribute("products", productsPayload);
		model.addAttribute("categoryPath", categoryPath);
		
		return new ModelAndView("/products/product-category-list");
	}
	
	@RequestMapping(path = "/category-list")
	public ModelAndView goProductsRootCategory(ModelMap model) {
		
		ProductCategory category = categoryService.findByName("root");
		List<Product> products = productService.findAllProducts();
		
		List<ProductPayload> productsPayload = productService.createProductPayload(products);
		
		model.addAttribute("category", category);
		model.addAttribute("products", productsPayload);
		
		return new ModelAndView("/products/product-category-list");
	}
	
	@RequestMapping(path = "/{productId}/details")
	public ModelAndView goProductDetails(ModelMap model,
			@PathVariable("productId") Long productId) {
		
		Product product = productService.findById(productId);
		List<ProductCategory> productCategoriesPath = 
				productService.buildProductCategoryPath(product);
		
		model.addAttribute("product", product);
		model.addAttribute("productCategoriesPath", productCategoriesPath);
		
		productCategoriesPath.stream()
			.forEach(category -> System.out.println(category.getName() + " depth: " + category.getDepth()));
		
		return new ModelAndView("products/product-details", model);
	}
	
	

}
