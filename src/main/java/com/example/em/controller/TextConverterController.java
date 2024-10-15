package com.example.em.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/TextConverter")
public class TextConverterController {

	@GetMapping("/upload")
	public String TextConverterGet() {

		return "TextConverter";
	}

	@PostMapping("/upload")
	public String TextConverterPost(
			@RequestParam("file") MultipartFile file,
			@RequestParam("options") String encoding,
			RedirectAttributes redirectAttributes) {

		

		return "redirect:/TextConverter/upload";
	}
}
