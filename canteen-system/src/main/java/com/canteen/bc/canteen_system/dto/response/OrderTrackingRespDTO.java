package com.canteen.bc.canteen_system.dto.response;

import com.canteen.bc.canteen_system.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderTrackingRespDTO {
  private long orderId;
  private String remark;
  private List<OrderedItemDto> orderedItems;
  private List<OrderTrackingStatus> orderTrackingStatus;

  @Builder
  @Getter
  public static class OrderedItemDto {
    private Long menuItemId;
    private String name;
    private Integer quantity;
  }

  @Builder
  @Getter
  public static class OrderTrackingStatus {
    private LocalDateTime timestamp;
    private OrderStatus status;
  }
}
