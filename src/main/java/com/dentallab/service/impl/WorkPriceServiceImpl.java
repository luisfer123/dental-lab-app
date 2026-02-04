package com.dentallab.service.impl;

import org.springframework.transaction.annotation.Transactional;

import com.dentallab.service.WorkPriceService;

public class WorkPriceServiceImpl implements WorkPriceService {

	@Override
	@Transactional(readOnly = true)
	public boolean existsBasePriceForWork(Long workId) {
		// Implementation goes here
		return false;
	}

}
