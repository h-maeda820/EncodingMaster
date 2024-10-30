package com.example.em.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

		//拡張子を取得
		String extension = "";
		int lastDotIndex = uploadForm.getFile().getOriginalFilename().lastIndexOf('.');
		if (lastDotIndex != -1) {
			// 拡張子を取得
			extension = uploadForm.getFile().getOriginalFilename().substring(lastDotIndex + 1);
		}

		//ファイルを変換
		File convertedFile = null;

		//csvからxml
		if (extension.equals("csv") && uploadForm.getExtension().equals("xml")) {
			convertedFile = convertCsvToXml(uploadForm.getFile());
		}
		//xmlからcsv
		else if (extension.equals("xml") && uploadForm.getExtension().equals("csv")) {
			convertedFile = convertXmlToCsv(uploadForm.getFile());
		}

		//csvに変換してFile型をセッションに保存
		session.setAttribute("convertedFile", convertedFile);
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
		String newFileName = baseFileName + "." + uploadForm.getExtension();

		// ファイルリソースを作成
		FileSystemResource resource = new FileSystemResource(convertedFile.toPath());

		// HTTPヘッダーを設定
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + newFileName);

		// レスポンスを返す
		ResponseEntity<FileSystemResource> responseEntity = new ResponseEntity<>(resource, headers, HttpStatus.OK);

		// レスポンス後に一時ファイルを削除したい
		new Thread(() -> {
			try {
				Thread.sleep(1000); // 1秒待つ
				Files.deleteIfExists(convertedFile.toPath()); // 一時ファイルを削除
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		return responseEntity;

	}

	// CSVをXMLに変換する
	private File convertCsvToXml(MultipartFile file) throws IOException {

		// 一時ファイルを作成
		Path tempFile = Files.createTempFile("converted_", ".xml");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
				PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempFile))) {

			// XMLのルート要素を書き出し
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<records>");

			// CSVの1行目を読み込み、フィールド名にする
			String headerLine = reader.readLine();
			if (headerLine != null) {
				String[] headers = headerLine.split(",");

				// 各データ行をXMLに変換
				String dataLine;
				while ((dataLine = reader.readLine()) != null) {
					String[] values = dataLine.split(",");
					writer.println("  <record>");

					// 各フィールドに対応するXML要素を生成
					for (int i = 0; i < headers.length; i++) {
						String tagName = headers[i].trim();
						String value = "";
						if (i < values.length) {
							value = values[i].trim();
						}
						writer.printf("    <%s>%s</%s>%n", tagName, value, tagName);
					}
					writer.println("  </record>");
				}
			}

			writer.println("</records>");
		}

		return tempFile.toFile();
	}

	// CSVをXMLに変換する
	private File convertXmlToCsv(MultipartFile file) throws IOException {
		// 一時ファイルを作成
		Path tempFile = Files.createTempFile("converted_", ".csv");

		// BufferedWriterを初期化
		try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
			// BOMを追加
			writer.write("\uFEFF"); // UTF-8 BOMを追加

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file.getInputStream());

			// ルート要素を取得
			Element root = document.getDocumentElement();

			// CSV用のWriterを準備
			// ヘッダーの作成: 最初のレコードからフィールド名を取得
			NodeList records = root.getElementsByTagName("record");
			if (records.getLength() > 0) {
				Element firstRecord = (Element) records.item(0);
				NodeList childNodes = firstRecord.getChildNodes();

				// ヘッダー行を作成
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node childNode = childNodes.item(i);
					if (childNode instanceof Element) {
						writer.write(childNode.getNodeName());
						if (i < childNodes.getLength() - 2) {
							writer.write(",");
						}
					}
				}
				writer.newLine(); // ヘッダー行の後に改行
			}

			// 各レコードを処理
			for (int i = 0; i < records.getLength(); i++) {
				Element record = (Element) records.item(i);
				NodeList childNodes = record.getChildNodes();

				// 各フィールドの値を取得
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node childNode = childNodes.item(j);
					if (childNode instanceof Element) {
						writer.write(childNode.getTextContent());
						if (j < childNodes.getLength() - 2) {
							writer.write(",");
						}
					}
				}
				writer.newLine(); // 各レコードの後に改行
			}
		} catch (ParserConfigurationException e) {
			System.err.println("Parserの設定に問題があります: " + e.getMessage());
		} catch (SAXException e) {
			System.err.println("XMLの解析に問題があります: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("ファイルの入出力に問題があります: " + e.getMessage());
		}

		return tempFile.toFile(); // 変換後のファイルを返す
	}

}
