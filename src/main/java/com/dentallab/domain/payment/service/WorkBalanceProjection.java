package com.dentallab.domain.payment.service;

import com.dentallab.domain.payment.model.WorkBalance;

public interface WorkBalanceProjection {

    WorkBalance project(Long workId);
}
