package com.dental.lab.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductCategory;
import com.dental.lab.model.payloads.ProductPayload;
import com.dental.lab.services.ProductCategoryService;
import com.dental.lab.services.ProductService;

@Controller
@RequestMapping(path = "/products")
public class ProductController {
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductCategoryService categoryService;
	
	/**
	 * Calls the {@linkplain ProductController#goProductsCategory(ModelMap model, Long categoryId)} 
	 * method with the {@code categoryId} of the root Category.
	 * Categories are expected to represent a well form tree, so there must be only
	 * one root category with attribute {@code depth = 0} and {@code parent = null}. 
	 * @return 
	 */
	@RequestMapping(path = "/category-list")
	public ModelAndView goProductsRootCategory() {
		
		ProductCategory category = 
				categoryService.findRootCategory();
		return new ModelAndView("redirect:/products/" + category.getId() + "/list");
	}
	
	/**
	 * Used to show in the view all {@linkplain Product}s which are related with the 
	 * {@linkplain ProductCategory} entity with id {@code categoryId}
	 * passed as parameter to this method.
	 * 
	 * @param model Spring {@linkplain ModelMap } object
	 * @param categoryId A Category's id.
	 * 					 It's used to retrieve the corresponding Category
	 * 					 entity from database and all the products which
	 * 					 have such category.
	 * @return {@linkplain ModelAndView} object containing: <br />
	 * 		   - {@code category }: An object of type {@linkplain ProductCategory} with id {@code categoryId} <br />
	 * 		   - {@code products }: A List of {@linkplain ProductPayload} representing all the products which
	 * 			 are related with the {@linkplain ProductCategory} with id {@code categoryId} <br />
	 * 		   - {@code categoryPath }: A List of {@linkplain ProductCategory } objects with
	 * 			 all super categories of the ProductCategory with id {@code categoryId}
	 */
	@RequestMapping(path = "/{categoryId}/list")
	public ModelAndView goProductsCategory(ModelMap model,
			@PathVariable("categoryId") Long categoryId) {
				
		ProductCategory category = categoryService.findById(categoryId);
		List<Product> products = 
				productService.findByCategoryId(categoryId);
		List<ProductPayload> productsPayload = 
				productService.createProductPayload(products);
		List<ProductCategory> categoryPath = 
				productService.buildCategoryPath(category);
		
		model.addAttribute("category", category);
		model.addAttribute("products", productsPayload);
		model.addAttribute("categoryPath", categoryPath);
		
		return new ModelAndView("/products/product-category-list");
	}
	
	/**
	 * Used to show in the view one specific {@linkplain Product} with all
	 * its attributes.
	 * 
	 * @param model Spring's {@linkplain ModelMap }
	 * @param productId The id of the {@linkplain Product } entity to be retrieve
	 * @return {@linkplain ModelAndView } object containing: <br /> 
	 * 			- {@code product}: An instance of {@linkplain ProductPayload } class representing
	 * 			the {@linkplain Product} object with id {@code productId} <br />
	 * 			- {@code productCategoriesPath}: A {@code List} of {@linkplain ProductCategory}
	 * 			objects containing all the {@code ProductCategy} objects related with the
	 * 			{@code Product} with id {@code productId }. {@code ProductCategory} are ordered
	 * 			by parent first than child.
	 */
	@RequestMapping(path = "/{productId}/details")
	public ModelAndView goProductDetails(ModelMap model,
			@PathVariable("productId") Long productId) {
		
		Product product = productService.findById(productId);
		ProductPayload productPayload = new ProductPayload(product, 
				productService.findCurrentPriceByProductId(productId).getPrice());
		List<ProductCategory> productCategoriesPath = 
				productService.buildProductCategoryPath(product);
		
		model.addAttribute("product", productPayload);
		model.addAttribute("productCategoriesPath", productCategoriesPath);
		
		productCategoriesPath.stream()
			.forEach(category -> System.out.println(category.getName() + " depth: " + category.getDepth()));
		
		return new ModelAndView("products/product-details", model);
	}
	
	

}
