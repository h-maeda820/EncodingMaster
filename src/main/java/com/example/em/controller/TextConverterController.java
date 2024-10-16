package com.example.em.controller;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
			@RequestParam("options") String charset,
			RedirectAttributes redirectAttributes) throws IOException {

		try {
			// 文字コードを検出
			String detectedEncoding = detectEncoding(file);
			System.out.println(detectedEncoding);

			// ファイルの内容を読み込み、指定された文字コードに変換
			byte[] fileContent = file.getBytes();
			String originalContent = new String(fileContent, detectedEncoding); // 元の文字コードを仮にUTF-8とする
			byte[] convertedContent = originalContent.getBytes(charset);

			// レスポンスとして変換後のファイルを返す
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(ContentDisposition.builder("attachment")
					.filename("converted.txt")
					.build());
			headers.setContentType(MediaType.TEXT_PLAIN);

			//return new ResponseEntity<>(convertedContent, headers, HttpStatus.OK);

		} catch (IOException e) {
			return "ファイルの処理中にエラーが発生しました: " + e.getMessage();
		}

		return "redirect:/TextConverter/upload";
	}

	public String detectEncoding(MultipartFile file) throws IOException {
		// ファイルをバイト配列に変換
		byte[] fileBytes = IOUtils.toByteArray(file.getInputStream());

		// UniversalDetector を使用して文字コードを検出
		UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(fileBytes, 0, fileBytes.length);
		detector.dataEnd();

		// 検出されたエンコーディングを取得
		String encoding = detector.getDetectedCharset();
		detector.reset();

		// エンコーディングが検出できなかった場合の処理
		if (encoding == null) {
			encoding = "????";
		}

		return encoding;
	}
}
