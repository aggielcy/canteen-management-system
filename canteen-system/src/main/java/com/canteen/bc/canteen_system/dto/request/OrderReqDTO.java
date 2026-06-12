package com.canteen.bc.canteen_system.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderReqDTO {
  private long userId;
  private LocalDateTime orderTime;
  private BigDecimal totalAmount;
  private List<OrderItemDto> items;
  private String paymentMethod;
  private String remark;

  @Getter
  @Setter
  public static class OrderItemDto {
    private Long menuItemId;
    private String name;
    private BigDecimal priceAtPurchase;
    private Integer quantity;
    private BigDecimal subTotal;
  }
}
