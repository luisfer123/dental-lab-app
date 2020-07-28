package com.dental.lab.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dental.lab.exceptions.AuthorityNotFoundException;
import com.dental.lab.exceptions.UserNotFoundException;
import com.dental.lab.model.entities.Authority;
import com.dental.lab.model.entities.Dentist;
import com.dental.lab.model.entities.User;
import com.dental.lab.model.enums.EAuthority;
import com.dental.lab.repositories.AuthorityRepository;
import com.dental.lab.repositories.DentistRepository;
import com.dental.lab.repositories.UserRepository;
import com.dental.lab.security.CustomUserDetails;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private AuthorityRepository authorityRepo;
	
	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private DentistRepository dentistRepo;
	
	@Transactional(readOnly = true)
	public User findByUsernameWithAuthorities(String username) 
			throws UsernameNotFoundException {
		
		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User with username: " + username + "does not exist"));
		
		List<Authority> authorities = 
				authorityRepo.findUserAuthoritiesByUsername(username);
		
		user.setAuthorities(authorities);
		
		return user;
	}
	
	@Transactional(readOnly = true)
	public User findByIdWithAuthorities(Long id) throws UserNotFoundException {
		
		User user = userRepo.findById(id).orElseThrow(
				() -> new UserNotFoundException("User with id: " + id + " was not found!"));
		
		List<Authority> authorities =
				authorityRepo.findUserAuthoritiesByUsername(user.getUsername());
		
		user.setAuthorities(authorities);
		
		return user;
	}
	
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return userRepo.existsByUsername(username);
	}
	
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepo.existsByEmail(email);
	}
	
	/**
	 * Creates a new user saving it in the database but does not logs in
	 * the new user.
	 * 
	 * @param newUser
	 * @return
	 * @throws EntityNotFoundException
	 */
	@Transactional
	public User createUser(User newUser) throws EntityNotFoundException {
		
		Authority userAuth = authorityRepo.findByAuthority(EAuthority.ROLE_USER)
				.orElseThrow(() -> new EntityNotFoundException());
		
		newUser.setAuthorities(Arrays.asList(userAuth));
		newUser.setPassword(encoder.encode(newUser.getPassword()));
		
		return userRepo.save(newUser);
	}
	
	/**
	 * Creates a new user by adding it to the database and also logs in the 
	 * new user into spring security context.
	 * 
	 * @param newUser user to be added to the database
	 * @return User entity that was saved
	 * @throws EntityNotFoundException
	 */
	@Transactional
	public User registerUser(User newUser) throws EntityNotFoundException {
		
		User user = createUser(newUser);
		
		UserDetails principal = CustomUserDetails.build(user);
		List<GrantedAuthority> authorities = user.getAuthorities()
				.stream()
				.map(authority -> new SimpleGrantedAuthority(authority.getAuthority().toString()))
				.collect(Collectors.toList());
		
		Authentication auth = new UsernamePasswordAuthenticationToken(
				principal, user.getPassword(), authorities);
		
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		return user;
	}
	
	@Transactional(readOnly = true)
	public List<User> findAll() {
		return userRepo.findAll();
	}
	
	@Transactional(readOnly = true)
	public User findById(Long id) throws UsernameNotFoundException {
		User user = userRepo.findById(id).orElseThrow(
				() -> new UsernameNotFoundException("User with id: " + id + " was not found"));
		
		return user;
	}
	
	@Transactional(readOnly = true)
	public List<User> searchByUsernameLike(String pattern) {
		List<User> users = userRepo.searchByUsernameLike(pattern);
		for(User user: users) {
			List<Authority> authorities = authorityRepo.findUserAuthoritiesByUsername(user.getUsername());
			user.setAuthorities(authorities);
		}
		
		return users;
	}
	
	@Transactional(readOnly = true)
	public List<User> searchByEmailLike(String pattern) {
		List<User> users = userRepo.searchByEmailLike(pattern);
		for(User user: users) {
			List<Authority> authorities = authorityRepo.findUserAuthoritiesByUsername(user.getUsername());
			user.setAuthorities(authorities);
		}
		
		return users;
	}
	
	@Transactional(readOnly = true)
	public List<User> searchByNameLike(String pattern) {
		List<User> users = userRepo.searchByNameLike(pattern);
		for(User user: users) {
			List<Authority> authorities = authorityRepo.findUserAuthoritiesByUsername(user.getUsername());
			user.setAuthorities(authorities);
		}
		
		return users;
	}
	
	@Transactional
	@PreAuthorize(value = "hasRole('ADMIN') or principal.id == #userId")
	public User updateProfilePicture(byte[] newProfilePicture, Long userId) {
		
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new RuntimeException());
		user.setProfilePicture(newProfilePicture);
		
		return userRepo.save(user);
	}
	
	@Transactional
	@PreAuthorize(value = "hasRole('ADMIN') or principal.id == #userId")
	public User updateUserInfo(Long userId, String username, String firstName, 
			String firstLastName, String secondLastName, String email) {
		
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " was not found"));
		
		user.setUsername(username);
		user.setFirstName(firstName);
		user.setFirstLastName(firstLastName);
		user.setSecondLastName(secondLastName);
		user.setEmail(email);
		
		return userRepo.save(user);
		
	}
	
	@Transactional
	@PreAuthorize(value = "hasRole('ADMIN')")
	public void deleteUserAuthority(Long userId, EAuthority authority)
			throws UserNotFoundException, AuthorityNotFoundException {
		
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " was not found!"));
		
		if(user.getUsername().equals("admin")) {
			throw new RuntimeException();
		}
		
		Authority auth = authorityRepo.findByAuthority(authority)
				.orElseThrow(() -> new AuthorityNotFoundException("Authority: " + authority + " was not found"));
		
		user.getAuthorities().remove(auth);
		userRepo.save(user);
		
		System.out.println("deleteUserAuthority() ecexuted!");
		
	}
	
	@Transactional
	@PreAuthorize(value = "hasRole('ADMIN')")
	public void addUserAuthority(Long userId, EAuthority authority)
			throws UserNotFoundException, AuthorityNotFoundException {
		
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " was not found!"));
		
		Authority auth = authorityRepo.findByAuthority(authority)
				.orElseThrow(() -> new AuthorityNotFoundException("Authority: " + authority + " was not found"));
		
		switch(authority) {
		case ROLE_CLIENT:
			if(!dentistRepo.existsByUserId(userId)) {
				Dentist dentist = new Dentist();
				dentist.setUser(user);
				user.setDentist(dentist);
			}
			break;
		case ROLE_TECHNICIAN:
			break;
		default:
			break;
		}
		
		user.getAuthorities().add(auth);
		userRepo.save(user);
				
	}
	
	@Transactional
	@PreAuthorize(value = "hasRole('ADMIN')")
	public void adminChangePassword(Long userId, String newPassword) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " was not found"));
		
		user.setPassword(encoder.encode(newPassword));
		userRepo.save(user);
	}

}
