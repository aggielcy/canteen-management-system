package com.canteen.bc.canteen_system.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuRespDTO {
  private String name;
  private Boolean isActive;
  private List<ItemRespDTO> items;

}
