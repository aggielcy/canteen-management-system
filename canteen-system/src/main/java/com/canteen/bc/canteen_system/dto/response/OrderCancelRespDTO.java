package com.canteen.bc.canteen_system.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderCancelRespDTO {
  private boolean isSuccess;
  private String message;
}
