package com.canteen.bc.canteen_system.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefundRequest {
  private Long orderId;
  private Long adminId;
}
