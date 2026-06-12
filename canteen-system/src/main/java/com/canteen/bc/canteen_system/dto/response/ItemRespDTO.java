package com.canteen.bc.canteen_system.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRespDTO {
  private Long itemId;
  private String name;
  private String description;
  private BigDecimal price;
  private String imageUrl;
  private Boolean isVisible;
  private Integer quantity;
  private List<Menu> menus;

  @Builder
  @Getter
  public static class Menu {
    private String name;
  }

}
