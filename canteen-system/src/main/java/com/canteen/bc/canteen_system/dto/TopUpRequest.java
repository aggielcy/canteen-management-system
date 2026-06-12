package com.canteen.bc.canteen_system.dto;

import java.math.BigDecimal;

import com.canteen.bc.canteen_system.model.PaymentMethod;

import lombok.Getter;

@Getter
public class TopUpRequest {
  private Long userId;
  private BigDecimal amount;
  private PaymentMethod paymentMethod;
  private String referenceId;
}
