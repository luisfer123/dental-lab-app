package com.dentallab.domain.payment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;
import com.dentallab.domain.payment.dto.PaymentAllocationCommand;
import com.dentallab.domain.payment.dto.RegisterPaymentRequest;
import com.dentallab.domain.payment.service.ClientBalanceService;
import com.dentallab.domain.payment.service.PaymentService;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.WorkFamilyRefEntity;
import com.dentallab.persistence.entity.WorkOrderEntity;
import com.dentallab.persistence.entity.WorkPriceEntity;
import com.dentallab.persistence.entity.WorkStatusRefEntity;
import com.dentallab.persistence.entity.WorkTypeRefEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.CrownWorkRepository;
import com.dentallab.persistence.repository.PaymentAllocationRepository;
import com.dentallab.persistence.repository.PaymentRepository;
import com.dentallab.persistence.repository.WorkFamilyRefRepository;
import com.dentallab.persistence.repository.WorkOrderRepository;
import com.dentallab.persistence.repository.WorkPriceRepository;
import com.dentallab.persistence.repository.WorkRepository;
import com.dentallab.persistence.repository.WorkStatusRefRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class PaymentIdempotencyIntegrationTest {

	@Autowired
	PaymentService paymentService;

	@Autowired
	ClientBalanceService clientBalanceService;

	@Autowired
	ClientRepository clientRepository;

	@Autowired
	WorkRepository workRepository;

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	PaymentAllocationRepository paymentAllocationRepository;

	@Autowired
	WorkFamilyRefRepository workFamilyRefRepository;

	@Autowired
	WorkTypeRefRepository workTypeRefRepository;

	@Autowired
	WorkStatusRefRepository workStatusRefRepository;

	@Autowired
	WorkOrderRepository workOrderRepository;
	
	@Autowired
	CrownWorkRepository crownWorkRepository;
	
	@Autowired
    WorkPriceRepository workPriceRepository;

	@Autowired
	EntityManager em;

	private WorkFamilyRefEntity family;
	private WorkTypeRefEntity type;
	private WorkStatusRefEntity status;

	@BeforeEach
	void setup() {

		family = workFamilyRefRepository.findById("FIXED_PROSTHESIS").orElseGet(() -> {
			WorkFamilyRefEntity f = new WorkFamilyRefEntity();
			f.setCode("FIXED_PROSTHESIS");
			f.setLabel("Fixed Prosthesis");
			return workFamilyRefRepository.save(f);
		});

		type = workTypeRefRepository.findById("CROWN").orElseGet(() -> {
			WorkTypeRefEntity t = new WorkTypeRefEntity();
			t.setCode("CROWN");
			t.setLabel("Crown");
			t.setFamily(family);
			return workTypeRefRepository.save(t);
		});

		status = workStatusRefRepository.findById("CREATED").orElseGet(() -> {
			WorkStatusRefEntity s = new WorkStatusRefEntity();
			s.setCode("CREATED");
			s.setLabel("Created");
			s.setSequenceOrder(nextStatusSequence()); // ✅ avoids uq_status_seq collision
			return workStatusRefRepository.save(s);
		});
	}

	private int nextStatusSequence() {
		Integer max = em
				.createQuery("select coalesce(max(s.sequenceOrder), 0) from WorkStatusRefEntity s", Integer.class)
				.getSingleResult();
		return max + 1;
	}

	@Test
	void registering_same_payment_twice_is_idempotent() {

		// ------------------------------------------------------------
		// 1. Create client (schema-valid)
		// ------------------------------------------------------------
		ClientEntity client = new ClientEntity();
		client.setFirstName("Test");
		client.setLastName("Client");
		client.setDisplayName("Idempotency Client");
		client.setActive(true);
		client.setCreatedAt(Instant.now());
		client.setUpdatedAt(Instant.now());
		client = clientRepository.save(client);

		// ------------------------------------------------------------
		// 2. Create client (schema-valid)
		// ------------------------------------------------------------
		WorkOrderEntity order = new WorkOrderEntity();
		order.setClient(client);
		order.setDateReceived(LocalDateTime.now());
		order = workOrderRepository.save(order);

		// ------------------------------------------------------------
		// 3. Create work
		// ------------------------------------------------------------
		WorkEntity work = new WorkEntity();
		work.setOrder(order);
		work.setClient(client);
		work.setWorkFamily(family);
		work.setType(type);
		work.setStatus(status);
		work.setCreatedAt(LocalDateTime.now());
		work.setUpdatedAt(LocalDateTime.now());

		workRepository.save(work);

		// ------------------------------------------------------------
		// Create crown_work extension (REQUIRED for pricing)
		// ------------------------------------------------------------
		// Create a material (needed for crown_work.core_material_id FK)
	     em.createNativeQuery("""
	         INSERT INTO material (name, category, unit, price_per_unit, status, notes)
	         VALUES ('Zirconia', 'CERAMIC', 'unit', 0.00, 'ACTIVE', NULL)
	     """).executeUpdate();
	
	     // Get the generated material_id (MySQL-specific, works in same connection)
	     Long materialId = (Long) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
	     
		CrownWorkEntity crown = new CrownWorkEntity();
		crown.setWork(work); // @MapsId → shares PK
		crown.setConstitution(FixProstheticConstitution.MONOLITHIC);
		crown.setBuildingTechnique(BuildingTechnique.DIGITAL);
		crown.setToothNumber("11");
		crown.setCoreMaterialId(materialId); // or a real material if pricing needs it
		crown.setNotes("Idempotency test crown");

		crownWorkRepository.save(crown);

		em.flush();
		em.clear();
		
		WorkPriceEntity wp = new WorkPriceEntity();
        wp.setWork(work);
        wp.setPrice(new BigDecimal("200.00"));
        wp.setCurrency("MXN");
        wp.setPriceGroup("DEFAULT");
        wp.setNotes("Seeded by PaymentFlowIntegrationTest");

        workPriceRepository.save(wp);
        
        em.flush();
		em.clear();

		// ------------------------------------------------------------
		// 4. Build payment request
		// ------------------------------------------------------------
		RegisterPaymentRequest request = new RegisterPaymentRequest();
		request.setClientId(client.getId());
		request.setPaymentAmount(new BigDecimal("100.00"));
		request.setIdempotencyKey("idem-test-001");
		request.setMoveRemainderToBalance(true);

		PaymentAllocationCommand alloc = new PaymentAllocationCommand();
		alloc.setWorkId(work.getId());
		alloc.setAllocatedAmount(new BigDecimal("60.00"));

		request.setAllocations(List.of(alloc));

		// ------------------------------------------------------------
		// 5. Execute payment TWICE
		// ------------------------------------------------------------
		paymentService.registerPayment(request);
		paymentService.registerPayment(request);

		// ------------------------------------------------------------
		// 6. Assert only ONE payment exists
		// ------------------------------------------------------------
		assertThat(paymentRepository.findAll()).hasSize(1);

		// ------------------------------------------------------------
		// 7. Assert only ONE allocation exists
		// ------------------------------------------------------------
		assertThat(paymentAllocationRepository.findAll()).hasSize(1);

		// ------------------------------------------------------------
		// 8. Assert balance credited ONCE
		// remainder = 100 - 60 = 40
		// ------------------------------------------------------------
		BigDecimal balance = clientBalanceService.getCurrentBalance(client.getId());

		assertThat(balance).isEqualByComparingTo("40.00");
	}
}
