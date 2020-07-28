package com.dental.lab.model.payloads;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductCategory;

public class ProductPayload {
	
	private Long id;
			
	private String name;
	
	private String description;
	
	private String productImage;
	
	private BigDecimal currentPrice;
	
	private List<ProductCategory> categories;

	public ProductPayload(Product product, BigDecimal currentPrice) {
		super();
		this.id = product.getId();
		this.name = product.getName();
		this.description = product.getDescription();
		this.productImage = Base64.getEncoder().encodeToString(product.getProductImage());
		this.currentPrice = currentPrice;
		this.categories = product.getCategories();
	}
	
	public ProductPayload(Product product) {
		super();
		this.id = product.getId();
		this.name = product.getName();
		this.description = product.getDescription();
		this.categories = product.getCategories();
		this.productImage = Base64.getEncoder().encodeToString(product.getProductImage());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProductImage() {
		return productImage;
	}

	public void setProductImage(String productImage) {
		this.productImage = productImage;
	}

	public BigDecimal getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(BigDecimal currentPrice) {
		this.currentPrice = currentPrice;
	}

	public List<ProductCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<ProductCategory> categories) {
		this.categories = categories;
	}
	

}
