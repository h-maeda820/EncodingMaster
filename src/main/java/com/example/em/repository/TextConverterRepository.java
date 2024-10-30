package com.example.em.repository;

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

		int result = 0;

		String sql = "INSERT INTO text_converter_log "
				+ "(file_name, original_bytes, converted_bytes, original_encoding, converted_encoding) "
				+ "VALUES (?, ?, ?, ?, ?)";

		Object[] param = { form.getFileName(), form.getOriginalBytes(), form.getConvertedBytes(),
				form.getOriginalEncoding(), form.getConvertedEncoding() };

		result += jdbcTemplate.update(sql, param);

		return result;

	}

}
