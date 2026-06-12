package com.canteen.bc.canteen_system.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ActiveMenusWithItemsResp {
    private boolean success;
    private String message;
    private List<MenuWithItemsDto> menus;
}

