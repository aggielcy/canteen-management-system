package com.canteen.bc.canteen_system.dto.request;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemReqDTO {
  private String name;
  private String description;
  private BigDecimal price;
  private String imageUrl;
  private Boolean isVisible;
  private Integer quantity;
  private List<Long> menuIds;
}
