package com.dental.lab.model.payloads;

import javax.persistence.Persistence;
import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.xml.crypto.Data;

import org.hibernate.type.Type;

import com.dental.lab.model.validation.CurrentPassword;
import com.dental.lab.model.validation.FieldMatch;

@GroupSequence({
	Type.class, 
	Data.class, 
	Persistence.class, 
	ChangePasswordPayload.class})
@FieldMatch(
		groups = Data.class, 
		first = "newPassword", 
		second = "confirmNewPassword", 
		message = "The newPassword and confirmNewPassword fields must match")
public class ChangePasswordPayload {
	
	@CurrentPassword
	private String currentPassword;

	@NotBlank
	private String newPassword;
	
	private String confirmNewPassword;
	
	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

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
