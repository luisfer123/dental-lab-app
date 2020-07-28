package com.dental.lab.model.payloads;

import javax.persistence.Persistence;
import javax.validation.GroupSequence;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.xml.crypto.Data;

import org.hibernate.type.Type;

import com.dental.lab.model.entities.User;
import com.dental.lab.model.validation.FieldMatch;
import com.dental.lab.model.validation.UniqueEmail;
import com.dental.lab.model.validation.UniqueUsername;

/**
 * 
 * @author Luis Fernando Martinez Oritz
 *
 */
@GroupSequence({
		Type.class, 
		Data.class, 
		Persistence.class, 
		RegisterUserPayload.class})
@FieldMatch(
		groups = Data.class, 
		first = "password", 
		second = "confirmPassword", 
		message = "The Password and Confirm Password fields must match.")
public class RegisterUserPayload {
	
	@NotBlank
	@UniqueUsername
	private String username;
	
	@NotBlank
	private String password;
	
	private String confirmPassword;
	
	@Email
	@NotBlank
	@UniqueEmail
	private String email;
	
	private String firstName;
	
	private String firstLastName;
	
	private String secondLastName;
	
	public User buildUser() {
		return new User(
				username, email, password, firstName, 
				firstLastName, secondLastName);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstLastName() {
		return firstLastName;
	}

	public void setFirstLastName(String firstLastName) {
		this.firstLastName = firstLastName;
	}

	public String getSecondLastName() {
		return secondLastName;
	}

	public void setSecondLastName(String secondLastName) {
		this.secondLastName = secondLastName;
	}

}
