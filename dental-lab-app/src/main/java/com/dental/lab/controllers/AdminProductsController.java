package com.dental.lab.controllers;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductPricing;
import com.dental.lab.services.ProductService;

@Controller
@RequestMapping(path = "/admin/products")
public class AdminProductsController {
	
	@Autowired
	private ProductService productService;
	
	@RequestMapping("/panel")
	public ModelAndView goAdminProductPanel() {
		return new ModelAndView("/admin/admin-products/admin-products");
	}
	
	@RequestMapping(path = "/products-list")
	public ModelAndView goProductsList(ModelMap model) {
		
		List<Product> products = productService.findAllProducts();
		model.addAttribute("products", products);
		
		return new ModelAndView("/admin/admin-products/products-list");
	}
	
	@RequestMapping(path = "/{product_id}/edit")
	public ModelAndView goEditProduct(ModelMap model,
			@PathVariable("product_id") Long productId) {
		
		Product product = productService.findById(productId);
		ProductPricing currentPrice = productService.findCurrentPrice(productId);
		String productImage = Base64.getEncoder().encodeToString(product.getProductImage());
		
		model.addAttribute("product", product);
		model.addAttribute("currentPrice", currentPrice);
		model.addAttribute("productImage", productImage);
		
		return new ModelAndView("/admin/admin-products/edit-product");
	}

}
