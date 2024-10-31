package com.example.em.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.em.models.FileConverterLogForm;
import com.example.em.repository.FileConverterRepository;

@Service
public class FileConverterService {

	/* fileConverterRepositoryクラス */
	private final FileConverterRepository repository;

	/* FileConverterServiceクラス */
	@Autowired
	public FileConverterService(FileConverterRepository repository) {
		this.repository = repository;
	}

	//履歴を追加
	public Integer insertLog(FileConverterLogForm form) {

		int result = 0;

		result += repository.insertLog(form);

		return result;

	}

	//履歴を取得
	public List<FileConverterLogForm> getLog() {

		return null;
	}

	//IDの履歴を取得
	public FileConverterLogForm getLogById(Integer id) {

		return null;
	}

	// IDの履歴を削除
	public Integer historyDelete(Integer id) {
		return null;
	}
}
