package com.dentallab.api.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dentallab.api.model.FullWorkOrderModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.api.model.WorkOrderModel;
import com.dentallab.service.WorkOrderService;
import com.dentallab.service.WorkService;

@RestController
@RequestMapping(
        value = "/api/orders",
        produces = "application/json"
)
public class WorkOrderController {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderController.class);

    private final WorkOrderService orderService;
    private final WorkService workService;

    public WorkOrderController(
    		WorkOrderService orderService,
    		WorkService workService) {
        this.orderService = orderService;
        this.workService = workService;
    }

    /* ============================================================
       GET ALL (paginated)
       ============================================================ */
    @GetMapping
    public ResponseEntity<Page<WorkOrderModel>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        log.debug("GET /api/orders?page={}&size={}&sort={}", page, size, sort);

        Page<WorkOrderModel> ordersPage =
                orderService.getAll(page, size, sort);

        log.info("GET /api/orders returned {} orders", ordersPage.getContent().size());

        return ResponseEntity.ok(ordersPage);
    }

    /* ============================================================
       GET BY ID (Full Model)
       ============================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<FullWorkOrderModel> getById(@PathVariable Long id) {
        log.debug("GET /api/orders/{} - fetching order", id);
        FullWorkOrderModel model = orderService.getById(id);
        log.info("GET /api/orders/{} succeeded", id);
        return ResponseEntity.ok(model);
    }
    
    /**
     * Get all works associated with a specific order ID
     * 	
     * @param id the order ID
     * @return a list of {@link WorkOrderModel} associated with the order
     * 
     */
    @GetMapping("/{id}/works")
    public ResponseEntity<List<WorkModel>> getWorksByOrderId(@PathVariable Long id) {
		log.debug("GET /api/orders/{}/works - fetching works for order", id);
		List<WorkModel> works = workService.getWorksByOrderId(id);
		log.info("GET /api/orders/{}/works succeeded", id);
		return ResponseEntity.ok(works);
	}

    /* ============================================================
       GET OVERDUE ORDERS
       ============================================================ */
    @GetMapping("/overdue")
    public ResponseEntity<Page<WorkOrderModel>> getOverdue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate,asc") String sort
    ) {
        log.warn("GET /api/orders/overdue?page={}&size={}&sort={}", page, size, sort);

        Page<WorkOrderModel> result =
                orderService.getOverdueOrders(page, size, sort);

        return ResponseEntity.ok(result);
    }

    /* ============================================================
       GET ORDERS DUE TODAY
       ============================================================ */
    @GetMapping("/due-today")
    public ResponseEntity<Page<WorkOrderModel>> getDueToday(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate,asc") String sort
    ) {
        log.debug("GET /api/orders/due-today?page={}&size={}&sort={}", page, size, sort);

        Page<WorkOrderModel> result =
                orderService.getDueToday(page, size, sort);

        return ResponseEntity.ok(result);
    }
    
    /* ============================================================
    GET ORDERS BY CLIENT ID
    ============================================================ */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<WorkOrderModel>> getByClientId(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        log.debug("GET /api/orders/client/{}?page={}&size={}&sort={}",
                clientId, page, size, sort);

        Page<WorkOrderModel> result =
                orderService.getByClientId(clientId, page, size, sort);

        log.info("GET client orders for client {} returned {} items",
                clientId, result.getTotalElements());

        return ResponseEntity.ok(result);
    }


    /* ============================================================
       CREATE ORDER DIRECTLY
       ============================================================ */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<FullWorkOrderModel> create(
            @RequestBody WorkOrderModel model
    ) {
        log.debug("POST /api/orders - creating new order for clientId={}", model.getClientId());

        FullWorkOrderModel created = orderService.create(model);

        log.info("Order created: id={}", created.getId());

        return ResponseEntity.ok(created);
    }

    /* ============================================================
       CREATE ORDER FOR CLIENT (Wizard Flow)
       ============================================================ */
    @PostMapping("/for-client/{clientId}")
    public ResponseEntity<FullWorkOrderModel> createForClient(@PathVariable Long clientId) {

        log.info("POST /api/orders/for-client/{} - creating order", clientId);

        FullWorkOrderModel created = orderService.createOrderForClient(clientId);

        return ResponseEntity.ok(created);
    }

    /* ============================================================
       UPDATE ORDER
       ============================================================ */
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<FullWorkOrderModel> update(
            @PathVariable Long id,
            @RequestBody WorkOrderModel model
    ) {
        log.debug("PUT /api/orders/{} - updating order", id);

        FullWorkOrderModel updated = orderService.update(id, model);

        log.info("Updated order {}", id);

        return ResponseEntity.ok(updated);
    }

    /* ============================================================
       MARK ORDER AS DELIVERED
       ============================================================ */
    @PostMapping("/{id}/deliver")
    public ResponseEntity<FullWorkOrderModel> deliver(@PathVariable Long id) {
        log.info("POST /api/orders/{}/deliver", id);

        FullWorkOrderModel delivered = orderService.markDelivered(id);

        return ResponseEntity.ok(delivered);
    }

    /* ============================================================
       DELETE ORDER
       ============================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("DELETE /api/orders/{} - deleting order", id);

        orderService.delete(id);

        log.info("Deleted order {}", id);

        return ResponseEntity.noContent().build();
    }
}
