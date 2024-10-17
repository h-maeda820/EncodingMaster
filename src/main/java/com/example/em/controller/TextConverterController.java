package com.example.em.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<FileSystemResource> TextConverterPost(
			@RequestParam("file") MultipartFile file,
			@RequestParam("options") String charset,
			RedirectAttributes redirectAttributes,
			HttpServletResponse response) throws IOException {

		// 現在の日時を取得してフォーマット
		String times = LocalDateTime.now().format(DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss"));

		System.out.println("-------------");
		// 文字コードを検出
		String detectedCharset = detectEncoding(file);
		System.out.println(detectedCharset);

		//テキストを読み込む
		String content = readFile(file, detectedCharset);
		System.out.println(content);
		
		//変換
		byte[] change = content.getBytes(Charset.forName("Shift_JIS"));

		// 指定された文字コードで変換
		///String converted = new String(content, Charset.forName("Shift_JIS"));

		// 新しいファイル名を作成
		//現在のファイル名を取得
		String originalFilename = file.getOriginalFilename();
		//ファイル名から拡張子を取り除く
		String baseFileName;
		if (originalFilename != null) {
			int lastDotIndex = originalFilename.lastIndexOf('.');
			if (lastDotIndex != -1) { // '.' が見つかった場合
				baseFileName = originalFilename.substring(0, lastDotIndex);
			} else {
				baseFileName = originalFilename; // '.' がない場合、元のファイル名を使用
			}
		} else {
			baseFileName = "download"; //originalFilenameがnullの場合
		}
		//ファイル名を作成
		String newFileName = baseFileName + "_" + charset + times + ".txt";

		// 一時ファイルを作成
		Path tempFile = Files.createTempFile("tempfile_", ".txt");
		try {

			// 一時ファイルに文字列を書き込む
			Files.write(tempFile, change, StandardOpenOption.WRITE);
			
			//テストここから
			String detectedCharset2 = detectEncoding(tempFile.toFile());
			System.out.println("変換後:" + detectedCharset2);
			String content2 = readFile(file, detectedCharset);
			System.out.println("変換後:\n"+content2.toString());
			System.out.println("-------------");
			//テストここまで
			
			// ファイルリソースを作成
			FileSystemResource resource = new FileSystemResource(tempFile);

			// HTTPヘッダーを設定
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + newFileName);

			// レスポンスを返す
			ResponseEntity<FileSystemResource> responseEntity = new ResponseEntity<>(resource, headers, HttpStatus.OK);

			// レスポンス後にファイルを削除したい
			new Thread(() -> {
				try {
					Thread.sleep(1000); // 1秒待つ
					Files.deleteIfExists(tempFile); // 一時ファイルを削除
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();

			return responseEntity;
		} catch (IOException e) {
			// エラーレスポンスを返す
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	//文字コードを判定する
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

	// 文字コードを判定する(テスト用)
	public String detectEncoding(File file) throws IOException {
		// ファイルをバイト配列に変換
		byte[] fileBytes = Files.readAllBytes(file.toPath());

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

	//テキスト読み込む
	private String readFile(MultipartFile file, String detectedCharset) throws IOException {
		// MultipartFileからInputStreamを取得
		try (InputStream inputStream = file.getInputStream()) {
			// InputStreamを指定された文字コードで読み込む
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream, Charset.forName(detectedCharset)));
			StringBuilder content = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				content.append(line).append(System.lineSeparator());
			}

			//			byte[] bytes = content.toString().getBytes(detectedCharset);

			//			return bytes;
			return content.toString();
		}
	}

	// テキストを読み込む(テスト用)
	private String readFile(File file, String detectedCharset) throws IOException {
		// ファイルからInputStreamを取得
		try (InputStream inputStream = Files.newInputStream(file.toPath())) {
			// InputStreamを指定された文字コードで読み込む
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream, Charset.forName(detectedCharset)));
			StringBuilder content = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				content.append(line).append(System.lineSeparator());
			}


			return content.toString();
		}
	}

}
