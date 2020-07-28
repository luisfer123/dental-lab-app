package com.dental.lab.model.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dental.lab.security.CustomUserDetails;

public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, String> {
	
	@Autowired
	private PasswordEncoder encoder;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		
		CustomUserDetails userDetails = ((CustomUserDetails) SecurityContextHolder
				.getContext()
				.getAuthentication()
				.getPrincipal());
		
		return encoder.matches(value, userDetails.getPassword());
	}

}
