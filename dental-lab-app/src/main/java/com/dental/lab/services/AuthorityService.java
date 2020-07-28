package com.dental.lab.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dental.lab.exceptions.AuthorityNotFoundException;
import com.dental.lab.model.entities.Authority;
import com.dental.lab.model.enums.EAuthority;
import com.dental.lab.repositories.AuthorityRepository;

@Service
public class AuthorityService {
	
	@Autowired
	private AuthorityRepository authRepo;
	
	public Authority findByAuthority(EAuthority authority) {
		return authRepo.findByAuthority(authority)
				.orElseThrow(() -> new AuthorityNotFoundException("Authority was not found!"));
	}

}
