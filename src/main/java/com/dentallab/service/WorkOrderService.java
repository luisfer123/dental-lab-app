package com.dentallab.service;

import org.springframework.data.domain.Page;

import com.dentallab.api.model.FullWorkOrderModel;
import com.dentallab.api.model.WorkOrderModel;

public interface WorkOrderService {

    FullWorkOrderModel create(WorkOrderModel model);

    FullWorkOrderModel createOrderForClient(Long clientId);

    FullWorkOrderModel getById(Long id);

    Page<WorkOrderModel> getAll(int page, int size, String sort);

    Page<WorkOrderModel> getByClientId(Long clientId, int page, int size, String sort);

    Page<WorkOrderModel> getOverdueOrders(int page, int size, String sort);

    Page<WorkOrderModel> getDueToday(int page, int size, String sort);
    
    FullWorkOrderModel update(Long id, WorkOrderModel model);

    FullWorkOrderModel markDelivered(Long id);

    void delete(Long id);
}
