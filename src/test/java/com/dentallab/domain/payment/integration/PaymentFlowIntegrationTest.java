package com.dentallab.domain.payment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
import com.dentallab.persistence.entity.WorkTypePriceEntity;
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
import com.dentallab.persistence.repository.WorkTypePriceRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class PaymentFlowIntegrationTest {

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
    WorkTypePriceRepository workTypePriceRepository;
    
    @Autowired
    WorkPriceRepository workPriceRepository;
    
    @Autowired
    CrownWorkRepository crownWorkRepository;
    
    @Autowired
    EntityManager em;
    
    private WorkFamilyRefEntity family;
    private WorkTypeRefEntity type;
    private WorkStatusRefEntity status;

    @BeforeEach
    void setup() {

        family = workFamilyRefRepository.findById("FIXED_PROSTHESIS")
            .orElseGet(() -> {
                WorkFamilyRefEntity f = new WorkFamilyRefEntity();
                f.setCode("FIXED_PROSTHESIS");
                f.setLabel("Fixed Prosthesis");
                return workFamilyRefRepository.save(f);
            });

        type = workTypeRefRepository.findById("CROWN")
            .orElseGet(() -> {
                WorkTypeRefEntity t = new WorkTypeRefEntity();
                t.setCode("CROWN");
                t.setLabel("Crown");
                t.setFamily(family);
                return workTypeRefRepository.save(t);
            });

        status = workStatusRefRepository.findById("CREATED")
    	    .orElseGet(() -> {
    	        WorkStatusRefEntity s = new WorkStatusRefEntity();
    	        s.setCode("CREATED");
    	        s.setLabel("Created");
    	        s.setSequenceOrder(nextStatusSequence()); // ✅ avoids uq_status_seq collision
    	        return workStatusRefRepository.save(s);
    	    });
    }
    
    private int nextStatusSequence() {
        Integer max = em.createQuery(
            "select coalesce(max(s.sequenceOrder), 0) from WorkStatusRefEntity s",
            Integer.class
        ).getSingleResult();
        return max + 1;
    }

    @Test
    void payment_flow_allocates_and_credits_balance_correctly() {

        // ------------------------------------------------------------
        // 1. Create client and work order
        // ------------------------------------------------------------
    	ClientEntity client = new ClientEntity();
    	client.setFirstName("Test");
    	client.setLastName("Client");
    	client.setDisplayName("Integration Test Client");
    	client.setActive(true);
    	client.setCreatedAt(Instant.now());
    	client.setUpdatedAt(Instant.now());

    	client = clientRepository.save(client);
    	
    	WorkOrderEntity order = new WorkOrderEntity();
        order.setClient(client);
        order.setDateReceived(LocalDateTime.now());
        order = workOrderRepository.save(order);

        // ------------------------------------------------------------
        // 2. Create works
        // ------------------------------------------------------------
        WorkEntity work1 = new WorkEntity();
        work1.setClient(client);
        work1.setCreatedAt(LocalDateTime.now());
        work1.setWorkFamily(family);
        work1.setType(type);
        work1.setStatus(status);
        work1.setOrder(order);
        
        work1 = workRepository.save(work1);
        em.flush();
        em.clear(); // optional but very clarifying

        WorkEntity work2 = new WorkEntity();
        work2.setClient(client);
        work2.setWorkFamily(family);
        work2.setType(type);
        work2.setStatus(status);
        work2.setOrder(order);
        work2.setCreatedAt(LocalDateTime.now());
        
        work2 = workRepository.save(work2);
        em.flush();
        em.clear(); // optional but very clarifying
        
	     // ------------------------------------------------------------
	     // 2.1 Create required extension rows for pricing (crown_work)
	     // ------------------------------------------------------------
	
	     // Create a material (needed for crown_work.core_material_id FK)
	     em.createNativeQuery("""
	         INSERT INTO material (name, category, unit, price_per_unit, status, notes)
	         VALUES ('Zirconia', 'CERAMIC', 'unit', 0.00, 'ACTIVE', NULL)
	     """).executeUpdate();
	
	     // Get the generated material_id (MySQL-specific, works in same connection)
	     Number materialId = (Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
	
	     // Insert crown_work for work1 (and work2 if you want it consistent)
	     em.createNativeQuery("""
	         INSERT INTO crown_work (
	             work_id,
	             tooth_number,
	             constitution,
	             building_technique,
	             building_status_id,
	             core_material_id,
	             veneering_material_id,
	             notes
	         )
	         VALUES (?, '11', 'MONOLITHIC', 'DIGITAL', NULL, ?, NULL, NULL)
	     """)
	     .setParameter(1, work1.getId())
	     .setParameter(2, materialId.longValue())
	     .executeUpdate();
	
	     em.createNativeQuery("""
	         INSERT INTO crown_work (
	             work_id,
	             tooth_number,
	             constitution,
	             building_technique,
	             building_status_id,
	             core_material_id,
	             veneering_material_id,
	             notes
	         )
	         VALUES (?, '21', 'MONOLITHIC', 'DIGITAL', NULL, ?, NULL, NULL)
	     """)
	     .setParameter(1, work2.getId())
	     .setParameter(2, materialId.longValue())
	     .executeUpdate();
	
	     em.flush();
	     em.clear();

        // ------------------------------------------------------------
        // 3. Build payment request
        // ------------------------------------------------------------
        WorkTypePriceEntity price = new WorkTypePriceEntity();
        price.setWorkFamily("FIXED_PROSTHESIS");
        price.setWorkType("CROWN");
        price.setConstitution("MONOLITHIC");
        price.setBuildingTechnique("DIGITAL");
        price.setCoreMaterialId(materialId.longValue());
        price.setPriceGroup("DEFAULT"); // or whatever your resolver expects
        price.setBasePrice(new BigDecimal("200.00"));
        price.setCurrency("MXN");
        price.setValidFrom(LocalDate.now().minusDays(1));

        workTypePriceRepository.save(price);
        em.flush();
        em.clear();
        
        WorkPriceEntity wp1 = new WorkPriceEntity();
        wp1.setWork(work1);
        wp1.setPrice(new BigDecimal("200.00"));
        wp1.setCurrency("MXN");
        wp1.setPriceGroup("DEFAULT");
        wp1.setNotes("Seeded by PaymentFlowIntegrationTest");

        workPriceRepository.save(wp1);

        WorkPriceEntity wp2 = new WorkPriceEntity();
        wp2.setWork(work2);
        wp2.setPrice(new BigDecimal("200.00"));
        wp2.setCurrency("MXN");
        wp2.setPriceGroup("DEFAULT");
        wp2.setNotes("Seeded by PaymentFlowIntegrationTest");

        workPriceRepository.save(wp2);

        em.flush();
        em.clear();

        
        RegisterPaymentRequest request = new RegisterPaymentRequest();
        request.setClientId(client.getId());
        request.setPaymentAmount(new BigDecimal("150.00"));
        request.setIdempotencyKey("payment-flow-it-001");
        request.setMoveRemainderToBalance(true);

        PaymentAllocationCommand allocation = new PaymentAllocationCommand();
        allocation.setWorkId(work1.getId());
        allocation.setAllocatedAmount(new BigDecimal("100.00"));

        request.setAllocations(List.of(allocation));

        // ------------------------------------------------------------
        // 4. Execute payment
        // ------------------------------------------------------------
        paymentService.registerPayment(request);

        // ------------------------------------------------------------
        // 5. Assert payment persisted
        // ------------------------------------------------------------
        assertThat(paymentRepository.findAll()).hasSize(1);

        var payment = paymentRepository.findAll().get(0);
        assertThat(payment.getAmountTotal())
            .isEqualByComparingTo("150.00");

        // ------------------------------------------------------------
        // 6. Assert allocation persisted
        // ------------------------------------------------------------
        assertThat(paymentAllocationRepository.findAll()).hasSize(1);

        var savedAllocation = paymentAllocationRepository.findAll().get(0);
        assertThat(savedAllocation.getWork().getId())
            .isEqualTo(work1.getId());
        assertThat(savedAllocation.getAmountApplied())
            .isEqualByComparingTo("100.00");

        // ------------------------------------------------------------
        // 7. Assert client balance credited with remainder
        // ------------------------------------------------------------
        BigDecimal balance =
            clientBalanceService.getCurrentBalance(client.getId());

        assertThat(balance)
            .isEqualByComparingTo("50.00");
    }
    
    @Test
    void payment_with_exact_allocation_creates_no_balance_movement() {

        // ------------------------------------------------------------
        // 1. Create client
        // ------------------------------------------------------------
        ClientEntity client = new ClientEntity();
        client.setFirstName("Exact");
        client.setLastName("Allocation");
        client.setDisplayName("Exact Allocation Client");
        client.setActive(true);
        client.setCreatedAt(Instant.now());
        client.setUpdatedAt(Instant.now());
        client = clientRepository.save(client);

        // ------------------------------------------------------------
        // 2. Create order
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
        // 4. Create crown_work extension (pricing requires it)
        // ------------------------------------------------------------
        em.createNativeQuery("""
            INSERT INTO material (name, category, unit, price_per_unit, status, notes)
            VALUES ('Zirconia', 'CERAMIC', 'unit', 0.00, 'ACTIVE', NULL)
        """).executeUpdate();

        Long materialId =
            ((Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult())
                .longValue();

        CrownWorkEntity crown = new CrownWorkEntity();
        crown.setWork(work);
        crown.setConstitution(FixProstheticConstitution.MONOLITHIC);
        crown.setBuildingTechnique(BuildingTechnique.DIGITAL);
        crown.setToothNumber("11");
        crown.setCoreMaterialId(materialId);
        crown.setNotes("Exact allocation test crown");

        crownWorkRepository.save(crown);

        // ------------------------------------------------------------
        // 5. Seed fixed base price (source of truth)
        // ------------------------------------------------------------
        WorkPriceEntity price = new WorkPriceEntity();
        price.setWork(work);
        price.setPrice(new BigDecimal("100.00"));
        price.setCurrency("MXN");
        price.setPriceGroup("DEFAULT");
        price.setNotes("Exact allocation test");

        workPriceRepository.save(price);

        em.flush();
        em.clear();

        // ------------------------------------------------------------
        // 6. Build payment request
        // ------------------------------------------------------------
        RegisterPaymentRequest request = new RegisterPaymentRequest();
        request.setClientId(client.getId());
        request.setPaymentAmount(new BigDecimal("100.00")); // EXACT
        request.setIdempotencyKey("exact-allocation-001");
        request.setMoveRemainderToBalance(true); // important branch

        PaymentAllocationCommand allocation = new PaymentAllocationCommand();
        allocation.setWorkId(work.getId());
        allocation.setAllocatedAmount(new BigDecimal("100.00")); // EXACT

        request.setAllocations(List.of(allocation));

        // ------------------------------------------------------------
        // 7. Execute payment
        // ------------------------------------------------------------
        paymentService.registerPayment(request);

        // ------------------------------------------------------------
        // 8. Assertions
        // ------------------------------------------------------------

        // One payment
        assertThat(paymentRepository.findAll()).hasSize(1);

        // One allocation
        assertThat(paymentAllocationRepository.findAll()).hasSize(1);

        // NO balance credit
        BigDecimal balance =
            clientBalanceService.getCurrentBalance(client.getId());

        assertThat(balance).isEqualByComparingTo("0.00");
    }
    
    @Test
    void payment_with_no_allocations_credits_full_amount_to_balance() {

        // ------------------------------------------------------------
        // 1. Create client
        // ------------------------------------------------------------
        ClientEntity client = new ClientEntity();
        client.setFirstName("Wallet");
        client.setLastName("Topup");
        client.setDisplayName("Wallet Topup Client");
        client.setActive(true);
        client.setCreatedAt(Instant.now());
        client.setUpdatedAt(Instant.now());
        client = clientRepository.save(client);

        // ------------------------------------------------------------
        // 2. Build payment request (NO allocations)
        // ------------------------------------------------------------
        RegisterPaymentRequest request = new RegisterPaymentRequest();
        request.setClientId(client.getId());
        request.setPaymentAmount(new BigDecimal("250.00"));
        request.setIdempotencyKey("wallet-topup-001");
        request.setMoveRemainderToBalance(true);
        request.setAllocations(List.of()); // 👈 explicit empty list

        // ------------------------------------------------------------
        // 3. Execute payment
        // ------------------------------------------------------------
        paymentService.registerPayment(request);

        // ------------------------------------------------------------
        // 4. Assertions
        // ------------------------------------------------------------

        // One payment persisted
        assertThat(paymentRepository.findAll()).hasSize(1);

        // No allocations created
        assertThat(paymentAllocationRepository.findAll()).isEmpty();

        // Full amount credited to balance
        BigDecimal balance =
            clientBalanceService.getCurrentBalance(client.getId());

        assertThat(balance).isEqualByComparingTo("250.00");
    }


}
