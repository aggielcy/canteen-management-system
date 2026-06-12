package com.canteen.bc.canteen_system.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdStatLogDTO {
  private Long logId;
  private Long orderId;
  private String customerName;
  private String customerSchoolId;
  private String status;
  private LocalDateTime changedAt;
  private String changedByName;
  private String changedBySchoolId;
}
