package com.dental.lab.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dental.lab.model.entities.ProductPricing;

public interface ProductPricingRepository extends JpaRepository<ProductPricing, Long> {
	
	@Query("select pp from ProductPricing pp "
			+ "join pp.product p "
			+ "where pp.startingDate = "
			+ "(select max(ipp.startingDate) from ProductPricing ipp join ipp.product ip where ip.id = :id)"
			+ "and p.id = :id")
	Optional<ProductPricing> findCurrentPriceByProductId(@Param("id") Long id);

}
