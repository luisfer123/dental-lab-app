package com.dental.lab.services;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dental.lab.model.entities.Product;
import com.dental.lab.model.entities.ProductCategory;
import com.dental.lab.repositories.ProductCategoryRepository;

@Service
public class ProductCategoryService {
	
	@Autowired
	private ProductCategoryRepository categoryRepo;
	
	@Transactional(readOnly = true)
	public ProductCategory findByName(String name) {
		return categoryRepo.findByName(name)
				.orElseThrow(() -> new EntityNotFoundException("ProductCategory with name: " + name + " was not found"));
	}
	
	@Transactional(readOnly = true)
	public ProductCategory findById(Long id) {
		return categoryRepo.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("ProductCategory with id: " + id + " was not found"));
	}
	
	@Transactional(readOnly = true)
	public ProductCategory findRootCategory() {
		return categoryRepo.findRootCategory()
				.orElseThrow(() -> new EntityNotFoundException("Root CategoryProduct was not found"));
	}
	
	@Transactional(readOnly = true)
	public List<ProductCategory> findByProduct(Product product) {
		
		return categoryRepo.findProductCategoriesByProductId(product.getId());
	}

}
