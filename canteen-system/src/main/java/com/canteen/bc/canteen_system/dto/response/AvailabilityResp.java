package com.canteen.bc.canteen_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AvailabilityResp {
    private boolean success;
    private String message;
    private Long menuId;
    private boolean isActive;
}
