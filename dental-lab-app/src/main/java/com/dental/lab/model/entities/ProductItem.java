package com.dental.lab.model.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Product_item")
public class ProductItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;
	
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;

	@OneToMany(mappedBy = "productItem", 
			fetch = FetchType.EAGER, 
			cascade = CascadeType.ALL)
	private List<ProductItemStatusRecord> statusRecords;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
	public List<ProductItemStatusRecord> getStatusRecords() {
		return statusRecords;
	}

	public void setStatusRecords(List<ProductItemStatusRecord> statusRecords) {
		this.statusRecords = statusRecords;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		
		if(o == null || o.getClass() != getClass())
			return false;
		
		ProductItem other = (ProductItem) o;
		return id != null &&
				id.equals(other.id);
		
	}
	
	@Override
	public int hashCode() {
		return 58;
	}

}
