package com.example.em.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.em.models.TextConverterLogForm;
import com.example.em.repository.TextConverterRepository;

@Service
public class TextConverterService {

	/* TextConverterRepositoryクラス */
	private final TextConverterRepository repository;

	/* TextConverterServiceクラス */
	@Autowired
	public TextConverterService(TextConverterRepository repository) {
		this.repository = repository;
	}

	//履歴を追加
	public Integer insertLog(TextConverterLogForm form) {

		int result = 0;

		result += repository.insertLog(form);

		return result;

	}

	//履歴を取得
	public List<TextConverterLogForm> getLog() {
		List<Map<String, Object>> logs = repository.getLogs();
		List<TextConverterLogForm> logForms = new ArrayList<>();

		for (Map<String, Object> log : logs) {
			TextConverterLogForm logForm = new TextConverterLogForm();
			logForm.setId(Integer.parseInt(String.valueOf(log.get("id"))));
			logForm.formatCreatedAt((Timestamp) log.get("created_at"));

			logForm.setFileName((String) log.get("file_name"));
			logForm.setOriginalEncoding((String) log.get("original_encoding"));
			logForm.setConvertedEncoding((String) log.get("converted_encoding"));

			logForm.setConvertedBom(Integer.parseInt((String) log.get("converted_bom")));
			// logFormsリストに追加
			logForms.add(logForm);
		}

		return logForms;
	}

	//IDの履歴を取得
	public TextConverterLogForm getLogById(Integer id) {
		Map<String, Object> log = repository.getLogById(id);

		TextConverterLogForm logForm = new TextConverterLogForm();

		logForm.setFileName((String) log.get("file_name"));
		logForm.setConvertedBytes((byte[]) log.get("converted_bytes"));
		logForm.setConvertedEncoding((String) log.get("converted_encoding"));
		logForm.setConvertedBom(Integer.parseInt((String) log.get("converted_bom")));

		return logForm;
	}

	// IDの履歴を削除
	public Integer historyDelete(Integer id) {
		return repository.historyDelete(id);
	}
}
