package com.example.em.models;

import org.springframework.web.multipart.MultipartFile;

public class TextConverterUploadForm {
	private MultipartFile file;
	private Boolean bomExist;
	private String charset;

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
