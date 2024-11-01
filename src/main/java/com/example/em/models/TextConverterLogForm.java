package com.example.em.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "text_converter_log")
public class TextConverterLogForm {

	@Id


	private Integer id;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "original_bytes")
	private byte[] originalBytes; // 変換前のバイト列

	@Column(name = "converted_bytes")
	private byte[] convertedBytes; // 変換後のバイト列

	@Column(name = "original_encoding")
	private String originalEncoding;//変換前コード

	@Column(name = "converted_encoding")
	private String convertedEncoding;//変換後コード

	@Column(name = "converted_bom")
	private Integer convertedBom; // BOMの有無 0:対象外 1:あり 2;なし

	@Column(name = "created_at")
	private String createdAt; // 作成日

	// デフォルトコンストラクタ
	public TextConverterLogForm() {
		this.fileName = "";
		this.originalBytes = new byte[0];
		this.convertedBytes = new byte[0];
		this.originalEncoding = "";
		this.convertedEncoding = "";
		this.createdAt = "";
		this.convertedBom = 0;
	}

	// コンストラクタ
	public TextConverterLogForm(String fileName, byte[] originalBytes, byte[] convertedBytes, String originalEncoding,
			String convertedEncoding, Integer hasBom) {
		this.fileName = fileName;
		this.originalBytes = originalBytes;
		this.convertedBytes = convertedBytes;
		this.originalEncoding = originalEncoding;
		this.convertedEncoding = convertedEncoding;
		this.convertedBom = hasBom;
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

	public String getOriginalEncoding() {
		return originalEncoding;
	}

	public void setOriginalEncoding(String originalEncoding) {
		this.originalEncoding = originalEncoding;
	}

	public String getConvertedEncoding() {
		return convertedEncoding;
	}

	public void setConvertedEncoding(String convertedEncoding) {
		this.convertedEncoding = convertedEncoding;
	}

	public Integer getConvertedBom() {
		return convertedBom;
	}

	public void setConvertedBom(Integer hasBom) {
		this.convertedBom = hasBom;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
