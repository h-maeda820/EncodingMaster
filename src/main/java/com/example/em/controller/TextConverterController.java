package com.example.em.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.em.models.TextConverterUploadForm;

@Controller
@RequestMapping("/TextConverter")
public class TextConverterController {

	@GetMapping("/view")
	public String TextConverterGet() {

		return "TextConverter";
	}

	@PostMapping("/convert")
	public String TextConverterPost(
			@ModelAttribute("textConverterUploadForm") TextConverterUploadForm uploadForm,
			RedirectAttributes redirectAttributes,
			HttpServletResponse response) throws IOException {

		String charset = uploadForm.getCharset();
		Boolean bomExist = uploadForm.getBomExist();

		//未入力チェック
		if (uploadForm.getFile().isEmpty() || uploadForm.getCharset().equals("unselected")) {
			
			redirectAttributes.addFlashAttribute("errorMessage", "ファイルまたは文字コードが未選択です");
			return "redirect:/TextConverter/view";
		}

		System.out.println("-------------");
		// 文字コードを検出
		String detectedCharset = detectEncoding(uploadForm.getFile());
		System.out.println(detectedCharset);

		if (detectedCharset.equals("E-001")) {
			redirectAttributes.addFlashAttribute("textConverterUploadForm", uploadForm);
			redirectAttributes.addFlashAttribute("errorMessage", "アップロードしたファイルの文字コードが識別できません");
			return "redirect:/TextConverter/view";
		}

		//テキストを読み込む
		String content = readFile(uploadForm.getFile(), detectedCharset);
		System.out.println("アップロード:\n" + content);

		//変換
		byte[] change = content.getBytes(Charset.forName(charset));

		// charsetがUTF-8, UTF-16LE, UTF-16BE, UTF-32LE, UTF-32BEの場合
		if (charset.equals("UTF-8") || charset.equals("UTF-16LE") || charset.equals("UTF-16BE")
				|| charset.equals("UTF-32LE") || charset.equals("UTF-32BE")) {

			//チェック入ってる場合
			if (bomExist != null && bomExist) {

				// 変換後にbomがあるかチェック
				if (detectBOM(change) == 0) {

					// BOMがない場合、BOMを取得
					byte[] bom = getBOM(charset);

					// BOMを先頭に追加するための新しい配列を作成
					byte[] result = new byte[bom.length + change.length];

					// BOMを先頭にコピー
					System.arraycopy(bom, 0, result, 0, bom.length);

					// 変換した内容をその後にコピー
					System.arraycopy(change, 0, result, bom.length, change.length);

					// changeを更新
					change = result;

				}
			}

		}

		redirectAttributes.addFlashAttribute("content", change);
		redirectAttributes.addFlashAttribute("textConverterUploadForm", uploadForm);

		return "redirect:/TextConverter/download";
	}

	//ダウンロード
	@GetMapping("/download")
	public ResponseEntity<FileSystemResource> Download(
			@ModelAttribute("textConverterUploadForm") TextConverterUploadForm uploadForm,
			@ModelAttribute("content") byte[] change,
			RedirectAttributes redirectAttributes,
			HttpServletResponse response) throws IOException {

		String fileName = uploadForm.getFile().getOriginalFilename();

		// 現在の日時を取得してフォーマット
		String times = LocalDateTime.now().format(DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss"));

		// 新しいファイル名を作成
		//ファイル名から拡張子を取り除く
		String baseFileName;
		if (fileName != null) {
			int lastDotIndex = fileName.lastIndexOf('.');
			if (lastDotIndex != -1) { // '.' が見つかった場合
				baseFileName = fileName.substring(0, lastDotIndex);
			} else {
				baseFileName = fileName; // '.' がない場合、元のファイル名を使用
			}
		} else {
			baseFileName = "download"; //originalFilenameがnullの場合
		}
		//ファイル名を作成
		String newFileName = baseFileName + "_" + uploadForm.getCharset() + times + ".txt";

		// 一時ファイルを作成
		Path tempFile = Files.createTempFile("tempfile_", ".txt");
		try {

			// 一時ファイルに文字列を書き込む
			Files.write(tempFile, change, StandardOpenOption.WRITE);

			// ファイルリソースを作成
			FileSystemResource resource = new FileSystemResource(tempFile);

			// HTTPヘッダーを設定
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + newFileName);

			// レスポンスを返す
			ResponseEntity<FileSystemResource> responseEntity = new ResponseEntity<>(resource, headers, HttpStatus.OK);

			// レスポンス後に一時ファイルを削除したい
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
			encoding = "E-001";
		}

		return encoding;
	}

	// BOMを判定する
	private int detectBOM(byte[] data) {
		int bomLength = 0;

		// BOMをチェック
		if (data.length >= 3 && data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) {
			bomLength = 3; // UTF-8 BOM
			System.out.println("UTF-8 BOM");
		} else if (data.length >= 2 && data[0] == (byte) 0xFE && data[1] == (byte) 0xFF) {
			bomLength = 2; // UTF-16 BE BOM
			System.out.println("UTF-16 BE BOM");
		} else if (data.length >= 2 && data[0] == (byte) 0xFF && data[1] == (byte) 0xFE) {
			bomLength = 2; // UTF-16 LE BOM
			System.out.println("UTF-16 LE BOM");
		} else if (data.length >= 4 && data[0] == (byte) 0x00 && data[1] == (byte) 0x00 && data[2] == (byte) 0xFE
				&& data[3] == (byte) 0xFF) {
			bomLength = 4; // UTF-32 BE BOM
			System.out.println("UTF-32 BE BOM");
		} else if (data.length >= 4 && data[0] == (byte) 0xFF && data[1] == (byte) 0xFE && data[2] == (byte) 0x00
				&& data[3] == (byte) 0x00) {
			bomLength = 4; // UTF-32 LE BOM
			System.out.println("UTF-32 LE BOM");
		} else {
			System.out.println("BOMなし");
		}

		return bomLength;
	}

	// テキスト読み込む
	private String readFile(MultipartFile file, String detectedCharset) throws IOException {
		try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {

			//BOMの長さを取得
			int bomLength = detectBOM(file.getBytes());

			// BOMをスキップ
			if (bomLength > 0) {
				inputStream.skip(bomLength);
			}

			// 指定された文字コードでInputStreamを読み込む
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

	// BOMを取得する
	private byte[] getBOM(String charset) {
		switch (charset) {
		case "UTF-8":
			return new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }; // UTF-8 BOM
		case "UTF-16BE":
			return new byte[] { (byte) 0xFE, (byte) 0xFF }; // UTF-16 BE BOM
		case "UTF-16LE":
			return new byte[] { (byte) 0xFF, (byte) 0xFE }; // UTF-16 LE BOM
		case "UTF-32BE":
			return new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF }; // UTF-32 BE BOM
		case "UTF-32LE":
			return new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 }; // UTF-32 LE BOM
		default:
			return new byte[0]; // BOMなし
		}
	}
}