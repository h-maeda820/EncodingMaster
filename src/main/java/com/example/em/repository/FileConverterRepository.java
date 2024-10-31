package com.example.em.repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.em.models.FileConverterLogForm;

@Repository
public class FileConverterRepository {
	/* JdbcTemplate */
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public FileConverterRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	//履歴を追加
	public Integer insertLog(FileConverterLogForm form) {
		String sql = "INSERT INTO file_converter_log "
				+ "(file_name, original_extension, converted_extension, original_bytes, converted_bytes) "
				+ "VALUES (?, ?, ?, ?, ?)";

		Object[] param = {
				form.getFileName(),
				form.getOriginalExtension(),
				form.getConvertedExtension(),
				form.getOriginalBytes(),
				form.getConvertedBytes(),
		};

		return jdbcTemplate.update(sql, param);
	}

	// 履歴を取得
	public List<Map<String, Object>> getLogs() {
		return null;
	}

	// IDの履歴を取得
	public Map<String, Object> getLogById(Integer id) {
		return null;
	}

	// IDの履歴を削除
	public Integer historyDelete(Integer id) {
		return null;
	}

}
