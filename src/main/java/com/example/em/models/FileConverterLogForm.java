package com.example.em.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "file_converter_log")
public class FileConverterLogForm {

	@Id
	private Integer id;

	@Column(name = "file_name")
	private String fileName; // 元のファイル名

	@Column(name = "original_extension")
	private String originalExtension; // 変換前の拡張子

	@Column(name = "converted_extension")
	private String convertedExtension; // 変換後の拡張子

	@Column(name = "original_bytes")
	private byte[] originalBytes; // 変換前のバイト列

	@Column(name = "converted_bytes")
	private byte[] convertedBytes; // 変換後のバイト列

	@Column(name = "created_at")
	private String createdAt; // 作成日

	// デフォルトコンストラクタ
	public FileConverterLogForm() {
		this.fileName = "";
		this.originalExtension = "";
		this.convertedExtension = "";
		this.originalBytes = new byte[0];
		this.convertedBytes = new byte[0];

		this.createdAt = "";

	}

	// コンストラクタ
	public FileConverterLogForm(String fileName, String originalExtension, String convertedExtension,
			byte[] originalBytes, byte[] convertedBytes,
			String originalEncoding, String convertedEncoding, Integer hasBom) {
		this.fileName = fileName;
		this.originalExtension = originalExtension;
		this.convertedExtension = convertedExtension;
		this.originalBytes = originalBytes;
		this.convertedBytes = convertedBytes;

	}

	// 作成日をフォーマットして登録する
	public void formatCreatedAt(Timestamp timestamp) {
		LocalDateTime localDateTime = timestamp.toLocalDateTime();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		this.createdAt = localDateTime.format(formatter);
	}

	// ゲッターとセッター
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOriginalExtension() {
		return originalExtension;
	}

	public void setOriginalExtension(String originalExtension) {
		this.originalExtension = originalExtension;
	}

	public String getConvertedExtension() {
		return convertedExtension;
	}

	public void setConvertedExtension(String convertedExtension) {
		this.convertedExtension = convertedExtension;
	}

	public byte[] getOriginalBytes() {
		return originalBytes;
	}

	public void setOriginalBytes(byte[] originalBytes) {
		this.originalBytes = originalBytes;
	}

	public byte[] getConvertedBytes() {
		return convertedBytes;
	}

	public void setConvertedBytes(byte[] convertedBytes) {
		this.convertedBytes = convertedBytes;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
