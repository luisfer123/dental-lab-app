package com.dental.lab.model.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.dental.lab.model.enums.EProductItemStatus;

/**
 * To require a new productItem, first a Product must be
 * chosen, the in the corresponding Oder, one new ProductItem
 * of that Product type will be created.
 * 
 * - Initially the new Product item will have a ORDERERD status.
 * - When some technician accepts to take that product item, its 
 *   status should change to ACCEPTED.
 * - When some technician starts to make that product item, its 
 *   status should change to IN_PROCCES.
 * - When the productItem is finished and ready to be delivered,
 *   its status should change to FINISHED.
 * - Finally, when the productItem is delivered to the Dentist
 *   (client) its status should change to DELIVERED.
 * 
 * 
 * @author Luis Fernando Martinez Oritz
 *
 */
@Entity
@Table(name = "Product_item_status_record")
public class ProductItemStatusRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "Product_item_id")
	private ProductItem productItem;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private EProductItemStatus status;
	
	@Column(name = "date")
	private Timestamp date;
	
	@Column(name = "note")
	private String note;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProductItem getProductItem() {
		return productItem;
	}

	public void setProductItem(ProductItem productItem) {
		this.productItem = productItem;
	}
	
	public EProductItemStatus getStatus() {
		return status;
	}

	public void setStatus(EProductItemStatus status) {
		this.status = status;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		
		if(o == null || o.getClass() != getClass())
			return false;
		
		ProductItemStatusRecord other = (ProductItemStatusRecord) o;
		return id != null &&
				id.equals(other.id);
		
	}
	
	@Override
	public int hashCode() {
		return 86;
	}

}
