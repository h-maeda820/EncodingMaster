package com.example.em.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.em.models.FileConverterUploadForm;

@Controller
@RequestMapping("/FileConverter")
public class FileConverterController {

	@GetMapping("/view")
	public String FileConverterGet() {

		return "FileConverter";
	}

	@PostMapping("/convert")
	public String FileConverterPost(
			@ModelAttribute("fileConverterUploadForm") FileConverterUploadForm uploadForm,
			RedirectAttributes redirectAttributes,
			HttpSession session,
			HttpServletResponse response) throws IOException {

		if (uploadForm.getFile().isEmpty() || uploadForm.getExtension().equals("")) {
			redirectAttributes.addFlashAttribute("fileConverterUploadForm", uploadForm);
			redirectAttributes.addFlashAttribute("errorMessage", "ファイルまたは拡張子が未選択です");
			return "redirect:/FileConverter/view";
		}

		// CSVをXMLに変換
		File xmlFile = convertCsvToXml(uploadForm.getFile());

		//csvに変換してFile型をセッションに保存
		session.setAttribute("convertedFile", xmlFile);

		return "redirect:/FileConverter/download";
	}

	@GetMapping("/download")
	public ResponseEntity<FileSystemResource> Download(
			@ModelAttribute("fileConverterUploadForm") FileConverterUploadForm uploadForm,
			@ModelAttribute("content") byte[] change,
			RedirectAttributes redirectAttributes, HttpSession session,
			HttpServletResponse response) throws IOException {

		// セッションからファイルを取得
		File convertedFile = (File) session.getAttribute("convertedFile");

		
		
		
		
		return null;
	}

	// CSVをXMLに変換する
	private File convertCsvToXml(MultipartFile file) throws IOException {
		List<String> lines = new BufferedReader(new InputStreamReader(file.getInputStream()))
				.lines()
				.collect(Collectors.toList());

		// XMLファイルの構築
		File tempFile = File.createTempFile("convertedFile", ".xml");
		PrintWriter writer = new PrintWriter(tempFile, "UTF-8");

		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<root>");

		for (String line : lines) {
			String[] columns = line.split(",");
			writer.println("  <record>");
			for (String column : columns) {
				writer.println("    <column>" + column.trim() + "</column>");
			}
			writer.println("  </record>");
		}

		writer.println("</root>");
		writer.close();

		return tempFile;
	}
}
