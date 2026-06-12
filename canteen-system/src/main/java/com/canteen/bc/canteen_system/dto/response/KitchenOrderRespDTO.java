package com.canteen.bc.canteen_system.dto.response;

import com.canteen.bc.canteen_system.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KitchenOrderRespDTO {
    private Long order_Id; 
  private String name; 
  private OrderStatus liveStatus; 

}
