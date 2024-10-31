package com.example.em.models;

import org.springframework.web.multipart.MultipartFile;

public class TextConverterUploadForm {
	private MultipartFile file;
	private Boolean bomExist;
	private String charset;

	// デフォルトコンストラクタ
	public TextConverterUploadForm() {
		this.file = null; // ファイルはnullで初期化
		this.bomExist = false; // BOMの存在をfalseで初期化
		this.charset = ""; // デフォルトの文字コードを設定
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public Boolean getBomExist() {
		return bomExist;
	}

	public void setBomExist(Boolean bomExist) {
		this.bomExist = bomExist;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

}
