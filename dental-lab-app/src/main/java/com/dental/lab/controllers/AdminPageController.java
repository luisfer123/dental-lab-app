package com.dental.lab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/admin")
public class AdminPageController {
	
	@RequestMapping(path = "")
	public String goAdminPage() {
		return "admin/admin";
	}

}
