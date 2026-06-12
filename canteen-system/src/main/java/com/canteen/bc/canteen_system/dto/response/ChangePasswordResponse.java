package com.canteen.bc.canteen_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordResponse {
  private boolean isSuccess;
  private String message;
}
