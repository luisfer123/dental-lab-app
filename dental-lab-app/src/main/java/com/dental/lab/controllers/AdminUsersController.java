package com.dental.lab.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dental.lab.exceptions.ImageNotValidException;
import com.dental.lab.model.entities.User;
import com.dental.lab.model.enums.EAuthority;
import com.dental.lab.model.payloads.AdminChangePasswordPayload;
import com.dental.lab.services.UserService;

@Controller
@RequestMapping(path = "/admin/users")
public class AdminUsersController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ResourceLoader resourceLoader;

	@RequestMapping(path = "/panel")
	public ModelAndView goAdminUsersPanel(ModelMap model) {
		return new ModelAndView("/admin/admin-users/admin-users");
	}

	@RequestMapping(path = "/list")
	public ModelAndView showUsers(ModelMap model) {

		List<User> users = userService.findAll();
		model.addAttribute("users", users);

		return new ModelAndView("admin/admin-users/users-list", model);
	}

	@RequestMapping(path = "/edit")
	public ModelAndView goEditUser(ModelMap model, @RequestParam(name = "user_id", required = false) Long userId) throws IOException {

		if (userId == null) {
			model.addAttribute("user", null);
			return new ModelAndView("admin/admin-users/edit-user", model);
		}

		User user = userService.findByIdWithAuthorities(userId);
		List<String> userAuthorities = user.getAuthorities()
				.stream()
				.map(authority -> authority.getAuthority().toString())
				.collect(Collectors.toList());
		
		// Use to populate the select menu in the Roles Admin tab
		List<String> userAuthoritiesComplement = new ArrayList<String>();
		for(EAuthority authority: EAuthority.values()) {
			if(!userAuthorities.contains(authority.toString())) {
				userAuthoritiesComplement.add(authority.toString());
			}
		}
		
		model.addAttribute("user", user);
		model.addAttribute("userAuthorities", userAuthorities);
		model.addAttribute("userAuthoritiesComplement", userAuthoritiesComplement);
		
		byte[] image;

		// If user does not have a profile picture we add a generic profile picture.
		if (user.getProfilePicture() == null || user.getProfilePicture().length <= 0) {

			Resource imageResource = resourceLoader.getResource("classpath:static/images/No-Photo-Placeholder.png");
			image = Files.readAllBytes(Paths.get(imageResource.getURI()));

		} else {
			image = user.getProfilePicture();

		}
		
		String profilePicture = Base64.getEncoder().encodeToString(image);
		model.addAttribute("profilePicture", profilePicture);

		return new ModelAndView("admin/admin-users/edit-user", model);
	}
	
	@RequestMapping(path = "/update/profilePicture", method = RequestMethod.POST)
	public ModelAndView updateProfilePicture(
			@RequestParam(name = "newProfilePicture", required = false) MultipartFile multipartFile,
			@RequestParam("user_id") Long userId) throws IOException, ImageNotValidException {
		
		byte[] newProfilePicture = null;
		User user;
		
		if(multipartFile != null && !multipartFile.isEmpty()) {
			newProfilePicture = multipartFile.getBytes();
			user = userService.updateProfilePicture(newProfilePicture, userId);
		} else {
			throw new ImageNotValidException("Chosen image is not valid!");
		}
		
		return new ModelAndView("redirect:/admin/users/edit?user_id=" + user.getId() + "&succes_updated=true");
	}
	
	@RequestMapping(value = "/{user_id}/update_general_info", method = RequestMethod.POST)
	public ModelAndView updateUserInfo(
			@Valid @ModelAttribute User userUpdated,
			@PathVariable("user_id") Long userId) {
		
		userService.updateUserInfo(userId, userUpdated.getUsername(), 
				userUpdated.getFirstName(), userUpdated.getFirstLastName(), 
				userUpdated.getSecondLastName(), userUpdated.getEmail());
		
		return new ModelAndView("redirect:/admin/users/edit?user_id=" + userId);
	}
	
	@RequestMapping(path = "/{user_id}/change_password", method = RequestMethod.POST)
	public ModelAndView changePassword(
			@Valid @ModelAttribute AdminChangePasswordPayload changePasswordPayload,
			@PathVariable(value = "user_id") Long userId) {
		
		userService.adminChangePassword(
				userId, changePasswordPayload.getNewPassword());
		
		return new ModelAndView("redirect:/admin/users/edit?user_id=" + userId);
	}
	
	@RequestMapping(path = "/{userId}/delete_role", method = RequestMethod.POST)
	public ModelAndView deleteAuthority(
			@RequestParam("authorityToDelete") EAuthority authorityToDelete,
			@PathVariable("userId") Long userId) {
								
		switch(authorityToDelete) {
		case ROLE_ADMIN:
			userService.deleteUserAuthority(userId, EAuthority.ROLE_ADMIN);
			break;
		case ROLE_CLIENT:
			userService.deleteUserAuthority(userId, EAuthority.ROLE_CLIENT);
			break;
		case ROLE_TECHNICIAN:
			userService.deleteUserAuthority(userId, EAuthority.ROLE_TECHNICIAN);
			break;
		default:
			break;
		}
		
		return new ModelAndView("redirect:/admin/users/edit?user_id=" + userId);
		
	}
	
	@RequestMapping(path = "/{userId}/add_role", method = RequestMethod.POST)
	public ModelAndView addAuthority(ModelMap model,
			@RequestParam("authorityToAdd") EAuthority authorityToAdd,
			@PathVariable("userId") Long userId) {
		
		userService.addUserAuthority(userId, authorityToAdd);
		
		return new ModelAndView("redirect:/admin/users/edit?user_id=" + userId);
	}

}
