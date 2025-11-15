package com.dentallab.service;

import java.util.List;
import com.dentallab.persistence.entity.WorkStatusRefEntity;

public interface WorkStatusRefService {

    List<WorkStatusRefEntity> getAllOrdered();

    WorkStatusRefEntity insertAfter(String newCode, String newLabel, String afterCode);

    WorkStatusRefEntity insertBefore(String newCode, String newLabel, String beforeCode);

    void reorder(List<String> orderedCodes);
}
