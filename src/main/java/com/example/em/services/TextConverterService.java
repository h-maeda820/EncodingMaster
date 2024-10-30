package com.example.em.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.em.models.TextConverterLogForm;
import com.example.em.repository.TextConverterRepository;

@Service
public class TextConverterService {

	/* CalendarRepositoryクラス */
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
}
