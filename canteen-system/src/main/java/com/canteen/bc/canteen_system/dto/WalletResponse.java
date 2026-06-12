package com.canteen.bc.canteen_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WalletResponse {
  private Long id;
  private Long userId;
  private String userName;
  private BigDecimal balance;
  private LocalDateTime updatedAt;
}
