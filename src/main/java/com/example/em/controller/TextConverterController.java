package com.example.em.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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

		try {
			InputStream stream = file.getInputStream();
			Reader reader = new InputStreamReader(stream, "UTF-8");
			BufferedReader buf = new BufferedReader(reader);

			String str;

			while ((str = buf.readLine()) != null) {

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return "redirect:/TextConverter/upload";
	}

}
