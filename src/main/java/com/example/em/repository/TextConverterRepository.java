package com.example.em.repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.em.models.TextConverterLogForm;

@Repository
public class TextConverterRepository {
	/* JdbcTemplate */
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public TextConverterRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	//履歴を追加
	public Integer insertLog(TextConverterLogForm form) {
		String sql = "INSERT INTO text_converter_log "
				+ "(file_name, original_bytes, converted_bytes, original_encoding, converted_encoding, converted_bom) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		Object[] param = {
				form.getFileName(),
				form.getOriginalBytes(),
				form.getConvertedBytes(),
				form.getOriginalEncoding(),
				form.getConvertedEncoding(),
				form.getConvertedBom()
		};

		return jdbcTemplate.update(sql, param);
	}

	// 履歴を取得
	public List<Map<String, Object>> getLogs() {
		String sql = "SELECT id, created_at, file_name, original_encoding, converted_encoding, converted_bom FROM text_converter_log";
		return jdbcTemplate.queryForList(sql);
	}

	// IDの履歴を取得
	public Map<String, Object> getLogById(Integer id) {
		String sql = "SELECT file_name, converted_bytes, converted_encoding, converted_bom FROM text_converter_log WHERE id = ?";
		return jdbcTemplate.queryForMap(sql, id);
	}

	// IDの履歴を削除
	public Integer historyDelete(Integer id) {
		String sql = "DELETE FROM text_converter_log WHERE id = ?";
		return jdbcTemplate.update(sql, id);
	}

}
