package com.dental.lab.model.payloads;

import javax.persistence.Persistence;
import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.xml.crypto.Data;

import org.hibernate.type.Type;

import com.dental.lab.model.validation.FieldMatch;

@GroupSequence({
	Type.class, 
	Data.class, 
	Persistence.class, 
	AdminChangePasswordPayload.class})
@FieldMatch(
		groups = Data.class, 
		first = "newPassword", 
		second = "confirmNewPassword", 
		message = "The newPassword and confirmNewPassword fields must match")
public class AdminChangePasswordPayload {
	
	@NotBlank
	private String newPassword;
	
	private String confirmNewPassword;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}
	
	

}
