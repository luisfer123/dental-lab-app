package com.dental.lab.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dental.lab.exceptions.ImageNotValidException;
import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductPricing;
import com.dental.lab.services.ProductService;

@Controller
@RequestMapping(path = "/admin/products")
public class AdminProductController {
	
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
	
	/**
	 * Updates the {@code productImage} field in the {@linkplain Product} 
	 * entity with id {@code productId}.
	 * 
	 * @param model Spring's {@linkplain ModelMap} object.
	 * @param productId Id of the product which image is going to be updated.
	 * @param multipartFile Contains the image to be used to update the {@code productImage} field.
	 * @return Stpring's {@linkplain ModelAndView} object redirecting to {@code /admin/products/productId/edit}.
	 * @throws IOException
	 * @throws ImageNotValidException
	 */
	@RequestMapping(
			path = "/{productId}/updateImage",
			method = RequestMethod.POST)
	public ModelAndView updateProductThumbnailImage(ModelMap model,
			@PathVariable("productId") Long productId,
			@RequestParam(name = "imageToUpdate", required = false) MultipartFile multipartFile) 
					throws IOException, ImageNotValidException {
		
		System.out.println(multipartFile);
		
		if(multipartFile.isEmpty())
			throw new ImageNotValidException("The chosen image is not valid!");
		
		byte[] imageToUpdate = multipartFile.getBytes();
		productService.updateProductThumbnailImage(productId, imageToUpdate);
		
		return new ModelAndView("redirect:/admin/products/" + productId + "/edit", model);
	}
	
	@RequestMapping(path = "/{productId}/updateProduct")
	public ModelAndView updateProduct(ModelMap model,
			@PathVariable("productId") Long productId,
			@RequestParam("newPrice") BigDecimal newPrice,
			@ModelAttribute Product updatedProduct) {
		
		/* 
		 * TODO: Validating that newPrice is different to current price and it
		 * is not negative. 
		 */
		productService.updateProduct(productId, updatedProduct, newPrice);
		
		return new ModelAndView("redirect:/admin/products/" + productId + "/edit", model);
		
	}

}
