package com.example.em.models;

import org.springframework.web.multipart.MultipartFile;

public class FileConverterUploadForm {

	private MultipartFile file;
	private String extension;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

}
