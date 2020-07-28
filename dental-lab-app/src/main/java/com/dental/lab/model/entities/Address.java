package com.dental.lab.model.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Address")
public class Address {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(strategy = "native", name = "native")
	private Long id;
	
	@OneToOne(mappedBy = "address")
	private ConsultingRoom consultingRoom;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ConsultingRoom getConsultingRoom() {
		return consultingRoom;
	}

	public void setConsultingRoom(ConsultingRoom consultingRoom) {
		this.consultingRoom = consultingRoom;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this)
			return true;
		
		if(o == null || o.getClass() != getClass())
			return false;
		
		Address other = (Address) o;
		return id != null &&
				id.equals(other.id);
	}
	
	@Override
	public int hashCode() {
		return 68;
	}
}
