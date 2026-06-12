package com.canteen.bc.canteen_system.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class MenuWithItemsDto{
  private Long menuId;
  private String menuName;
  private Boolean isActive;
  private List<ItemDTO> items;

}
