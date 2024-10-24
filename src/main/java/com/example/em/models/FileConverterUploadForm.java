package com.example.em.models;

import org.springframework.web.multipart.MultipartFile;

public class FileConverterUploadForm {

	MultipartFile file;
	String extension;

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
