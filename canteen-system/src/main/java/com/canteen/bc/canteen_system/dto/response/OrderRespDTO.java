package com.canteen.bc.canteen_system.dto.response;

import com.canteen.bc.canteen_system.model.OrderStatus;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderRespDTO {
  private long orderId;
  private String customerName;
  private String schoolId;
  private LocalDateTime orderTime;
  private BigDecimal totalAmount;
  private List<OrderedItemDto> orderedItems;
  private String remark;
  private OrderStatus orderStatus;
  private PaymentMethod paymentMethod;

  @Builder
  @Getter
  public static class OrderedItemDto {
    private Long menuItemId;
    private String name;
    private BigDecimal priceAtPurchase;
    private Integer quantity;
    private BigDecimal subTotal;
  }
}
