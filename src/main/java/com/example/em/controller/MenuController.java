package com.example.em.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

	// メニュー画面へ遷移
	@GetMapping("/EncodingMaster")
	public String showMenu() {
		return "menu";
	}
}
