package com.example.em.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
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
			@RequestParam(name = "bom", required = false) Boolean bomExist,
			@RequestParam("options") String charset,
			RedirectAttributes redirectAttributes,
			HttpServletResponse response) throws IOException {

		//未入力チェック
		
		

		// 現在の日時を取得してフォーマット
		String times = LocalDateTime.now().format(DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss"));

		System.out.println("-------------");
		// 文字コードを検出
		String detectedCharset = detectEncoding(file);
		System.out.println(detectedCharset);

		if (detectedCharset.equals("E-001")) {
			redirectAttributes.addFlashAttribute("errorMessage", "アップロードしたファイルの文字コードが識別できません");
			return ResponseEntity.status(HttpStatus.FOUND) // 302リダイレクト
					.location(URI.create("/TextConverter/upload")) // リダイレクト先のURI
					.build();
		}

		//テキストを読み込む
		String content = readFile(file, detectedCharset);
		System.out.println(content);

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

	// BOMを判定するメソッド
	private int detectBOM(InputStream inputStream) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		byte[] bom = new byte[4];
		bufferedInputStream.mark(bom.length); // ストリームの位置をマーク
		int readBytes = bufferedInputStream.read(bom, 0, bom.length);
		int bomLength = 0;

		// BOMをチェックして、スキップするバイト数を決定する
		if (readBytes >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
			bomLength = 3; // UTF-8 BOM
			System.out.println("UTF-8 BOM");
		} else if (readBytes >= 2 && bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
			bomLength = 2; // UTF-16 BE BOM
			System.out.println("UTF-16 BE BOM");
		} else if (readBytes >= 2 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
			bomLength = 2; // UTF-16 LE BOM
			System.out.println("UTF-16 LE BOM");
		} else if (readBytes >= 4 && bom[0] == (byte) 0x00 && bom[1] == (byte) 0x00 && bom[2] == (byte) 0xFE
				&& bom[3] == (byte) 0xFF) {
			bomLength = 4; // UTF-32 BE BOM
			System.out.println("UTF-32 BE BOM");
		} else if (readBytes >= 4 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == (byte) 0x00
				&& bom[3] == (byte) 0x00) {
			bomLength = 4; // UTF-32 LE BOM
			System.out.println("UTF-32 LE BOM");
		}

		// ストリームの位置をリセット
		bufferedInputStream.reset();

		return bomLength;
	}

	//bom判定変換後
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
		}

		return bomLength;
	}

	// テキストを読み込む
	private String readFile(MultipartFile file, String detectedCharset) throws IOException {
		// ファイルからInputStreamを取得
		try (InputStream inputStream = file.getInputStream()) {

			int bomLength = 0;

			if (detectedCharset.startsWith("UTF-")) {
				// BOMを判定
				bomLength = detectBOM(inputStream);
			}

			// BOMが存在する場合、そのバイト数をスキップする
			if (bomLength > 0) {
				inputStream.skip(bomLength);
			}

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

	// BOMを取得するメソッド
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