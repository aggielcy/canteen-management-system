package com.canteen.bc.canteen_system.dto.request;

import java.time.LocalDate;
import java.util.List;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
public class MenuReqDTO {
    private Long id;
    private String name;
    // private LocalDate menuDate;
    private Boolean isActive;
    private List<Item> items;
@Getter
    public static class Item{
        private Long itemId;
    }
}


