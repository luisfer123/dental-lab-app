package com.dental.lab.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dental.lab.model.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
	Optional<Product> findById(Long id);
	
	@Override
	@Query("select p from Product p left join fetch p.categories")
	List<Product> findAll();
	
	@Query("select p from Product p join p.categories c where c.id = :categoryId")
	List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

}
