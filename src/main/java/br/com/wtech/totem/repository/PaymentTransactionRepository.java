package br.com.wtech.totem.repository;

import br.com.wtech.totem.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD para transações de pagamento.
 * findByOrderId() ajuda a recuperar pela ordem PagBank.
 */
public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {
    PaymentTransaction findByOrderId(String orderId);
}
