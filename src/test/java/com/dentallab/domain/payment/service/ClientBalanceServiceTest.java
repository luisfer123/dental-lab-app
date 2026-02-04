package com.dentallab.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dentallab.domain.payment.service.impl.ClientBalanceServiceImpl;
import com.dentallab.persistence.entity.ClientBalanceEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.ClientBalanceMovementRepository;
import com.dentallab.persistence.repository.ClientBalanceRepository;
import com.dentallab.persistence.repository.WorkRepository;

@ExtendWith(MockitoExtension.class)
class ClientBalanceServiceTest {

    @Mock
    ClientBalanceRepository balanceRepository;

    @Mock
    ClientBalanceMovementRepository movementRepository;
    
    @Mock
    WorkRepository workRepository;

    @InjectMocks
    ClientBalanceServiceImpl service;

    private static final Long CLIENT_ID = 10L;

    ClientBalanceEntity activeBalance(BigDecimal amount) {
        ClientBalanceEntity cb = new ClientBalanceEntity();
        cb.setClientId(CLIENT_ID);
        cb.setAmount(amount);
        cb.setCurrency("MXN");
        cb.setActive(true);
        return cb;
    }

    // ----------------------------------------------------
    // getCurrentBalance
    // ----------------------------------------------------

    @Test
    void getCurrentBalance_returns_zero_when_no_balance_row_exists() {

        when(balanceRepository.findByClientId(CLIENT_ID))
            .thenReturn(Optional.empty());

        BigDecimal balance = service.getCurrentBalance(CLIENT_ID);

        assertThat(balance.scale()).isEqualTo(2);
        assertThat(balance).isEqualByComparingTo("0.00");

    }

    // ----------------------------------------------------
    // creditBalance
    // ----------------------------------------------------

    @Test
    void creditBalance_increases_balance_and_creates_ledger_entry() {

        ClientBalanceEntity cb = activeBalance(new BigDecimal("50.00"));
        when(balanceRepository.findByClientIdForUpdate(CLIENT_ID))
            .thenReturn(Optional.of(cb));

        service.creditBalance(
            CLIENT_ID,
            new BigDecimal("30.00"),
            "OVERPAYMENT",
            100L,
            null,
            "test credit"
        );

        assertThat(cb.getAmount()).isEqualByComparingTo("80.00");

        verify(movementRepository).save(argThat(m ->
            m.getAmountChange().compareTo(new BigDecimal("30.00")) == 0
            && m.getType().equals("OVERPAYMENT")
        ));

        verify(balanceRepository).save(cb);
    }

    @Test
    void creditBalance_rejects_non_positive_amount() {

        assertThatThrownBy(() ->
            service.creditBalance(
                CLIENT_ID,
                new BigDecimal("0.00"),
                "OVERPAYMENT",
                null,
                null,
                null
            )
        ).isInstanceOf(IllegalArgumentException.class);
        
        verify(balanceRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    // ----------------------------------------------------
    // applyBalanceToWork
    // ----------------------------------------------------

    @Test
    void applyBalanceToWork_decreases_balance_and_creates_negative_ledger_entry() {

        ClientBalanceEntity cb = activeBalance(new BigDecimal("100.00"));
        when(balanceRepository.findByClientIdForUpdate(CLIENT_ID))
            .thenReturn(Optional.of(cb));
        
        when(workRepository.findById(20L))
			.thenReturn(Optional.of(new WorkEntity()));

        service.applyBalanceToWork(
            CLIENT_ID,
            20L,
            new BigDecimal("40.00"),
            200L,
            "apply to work"
        );

        assertThat(cb.getAmount()).isEqualByComparingTo("60.00");

        verify(movementRepository).save(argThat(m ->
            m.getAmountChange().compareTo(new BigDecimal("-40.00")) == 0
            && m.getType().equals("APPLY_WORK")
        ));
        verify(balanceRepository).save(cb);

    }

    @Test
    void applyBalanceToWork_rejects_insufficient_balance() {

        ClientBalanceEntity cb = activeBalance(new BigDecimal("20.00"));
        when(balanceRepository.findByClientIdForUpdate(CLIENT_ID))
            .thenReturn(Optional.of(cb));

        assertThatThrownBy(() ->
            service.applyBalanceToWork(
                CLIENT_ID,
                20L,
                new BigDecimal("50.00"),
                null,
                null
            )
        ).isInstanceOf(IllegalStateException.class);

        verify(movementRepository, never()).save(any());
        verify(balanceRepository, never()).save(any());
    }

    // ----------------------------------------------------
    // recomputeBalanceCache
    // ----------------------------------------------------

    @Test
    void recomputeBalanceCache_overwrites_cached_amount_from_ledger() {

        ClientBalanceEntity cb = activeBalance(new BigDecimal("999.00"));
        when(balanceRepository.findByClientIdForUpdate(CLIENT_ID))
            .thenReturn(Optional.of(cb));

        when(movementRepository.sumLedgerBalanceByClientId(CLIENT_ID))
            .thenReturn(new BigDecimal("120.00"));

        service.recomputeBalanceCache(CLIENT_ID);

        assertThat(cb.getAmount()).isEqualByComparingTo("120.00");
        verify(balanceRepository).save(cb);
    }

    @Test
    void recomputeBalanceCache_rejects_negative_ledger_sum() {

        when(movementRepository.sumLedgerBalanceByClientId(CLIENT_ID))
            .thenReturn(new BigDecimal("-10.00"));
        
        when(balanceRepository.findByClientIdForUpdate(CLIENT_ID))
        .thenReturn(Optional.of(activeBalance(new BigDecimal("0.00"))));

        assertThatThrownBy(() ->
            service.recomputeBalanceCache(CLIENT_ID)
        ).isInstanceOf(IllegalStateException.class);
    }
}
