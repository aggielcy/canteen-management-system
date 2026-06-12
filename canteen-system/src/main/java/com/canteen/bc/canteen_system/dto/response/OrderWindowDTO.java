package com.canteen.bc.canteen_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderWindowDTO {
  private String openTime;
  private String closeTime;
}
