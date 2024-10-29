package com.example.em.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

		if (uploadForm.getFile().isEmpty() || uploadForm.getExtension().equals("unselected")) {

			redirectAttributes.addFlashAttribute("errorMessage", "ファイルまたは拡張子が未選択です");
			return "redirect:/FileConverter/view";
		}

		// 文字コードを検出
		String detectedCharset = detectEncoding(uploadForm.getFile());
		System.out.println(detectedCharset);

		if (detectedCharset.equals("E-001")) {
			redirectAttributes.addFlashAttribute("textConverterUploadForm", uploadForm);
			redirectAttributes.addFlashAttribute("errorMessage", "アップロードしたファイルの文字コードが識別できません");
			return "redirect:/TextConverter/view";
		}

		// CSVをXMLに変換
		File xmlFile = convertCsvToXml(uploadForm.getFile(),detectedCharset);

//		Path xmlFile = Files.createTempFile("tempfile_", ".xml");

		//csvに変換してFile型をセッションに保存
		session.setAttribute("convertedFile", xmlFile);
		redirectAttributes.addFlashAttribute("fileConverterUploadForm", uploadForm);

		return "redirect:/FileConverter/download";
	}

	@GetMapping("/download")
	public ResponseEntity<FileSystemResource> Download(
			@ModelAttribute("fileConverterUploadForm") FileConverterUploadForm uploadForm,
			//			@ModelAttribute("content") byte[] change,
			RedirectAttributes redirectAttributes, HttpSession session,
			HttpServletResponse response) throws IOException {

		// セッションからファイルを取得
		File convertedFile = (File) session.getAttribute("convertedFile");

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
		String newFileName = baseFileName + "_" + times + ".xml";

		// ファイルリソースを作成
		FileSystemResource resource = new FileSystemResource(convertedFile.toPath());

		// HTTPヘッダーを設定
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + newFileName);

		// レスポンスを返す
		ResponseEntity<FileSystemResource> responseEntity = new ResponseEntity<>(resource, headers, HttpStatus.OK);

		return responseEntity;

	}

	// CSVをXMLに変換する
	private File convertCsvToXml(MultipartFile file,String detectedCharset) throws IOException {

		return;
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
}
