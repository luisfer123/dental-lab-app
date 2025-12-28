package com.dentallab.domain.pricing.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.WorkPriceRepository;

import jakarta.persistence.EntityManager;

class WorkBasePriceServiceImplTest {
	
	@InjectMocks
	private WorkBasePriceServiceImpl service;

    @Mock
    private WorkTypePriceResolver workTypePriceResolver;

    @Mock
    private WorkPriceRepository workPriceRepository;
    
    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }
    
    @Test
    void previewBasePrice_delegatesToResolver() {

        Long workId = 1L;

        PriceResolutionRequest request =
                PriceResolutionRequest.forWork(workId);

        BasePriceResult expected =
                new BasePriceResult(
                        new BigDecimal("150.00"),
                        "MXN",
                        "DEFAULT",
                        10L
                );

        when(workTypePriceResolver.resolveBasePrice(request))
                .thenReturn(expected);

        BasePriceResult result = service.previewBasePrice(request);

        assertSame(expected, result);

        verify(workTypePriceResolver).resolveBasePrice(request);
        verifyNoInteractions(workPriceRepository);
    }

    @Test
    void fixBasePrice_persistsPreviewedBasePrice() {

        Long workId = 1L;
        
        WorkEntity work = new WorkEntity();
        work.setId(workId);

        when(entityManager.getReference(WorkEntity.class, workId))
                .thenReturn(work);


        BasePriceResult base =
                new BasePriceResult(
                        new BigDecimal("150.00"),
                        "MXN",
                        "DEFAULT",
                        10L
                );

        when(workPriceRepository.existsByWork_Id(workId))
                .thenReturn(false);

        service.fixBasePrice(workId, base);

        verify(workPriceRepository).save(argThat(entity ->
		        entity.getWork().getId().equals(workId) &&
		        entity.getPrice().compareTo(new BigDecimal("150.00")) == 0 &&
		        entity.getCurrency().equals("MXN") &&
		        entity.getPriceGroup().equals("DEFAULT")
		));
    }

    @Test
    void fixBasePrice_throwsIfAlreadyFixed() {

        Long workId = 1L;

        BasePriceResult base =
                new BasePriceResult(
                        new BigDecimal("150.00"),
                        "MXN",
                        "DEFAULT",
                        10L
                );

        when(workPriceRepository.existsByWork_Id(workId))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.fixBasePrice(workId, base)
        );

        verify(workPriceRepository, never()).save(any());
    }

}