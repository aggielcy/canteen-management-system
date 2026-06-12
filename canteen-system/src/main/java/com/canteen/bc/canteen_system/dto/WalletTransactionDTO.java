package com.canteen.bc.canteen_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.model.TrxStatus;
import com.canteen.bc.canteen_system.model.TrxType;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WalletTransactionDTO {
  private Long transactionId;
  private Long userId;
  private Long orderId;
  private PaymentMethod paymentMethod;
  private TrxType transactionType;
  private TrxStatus status;
  private BigDecimal amount;
  private BigDecimal balanceAfter;
  private LocalDateTime createdAt;
}
