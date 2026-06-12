package com.canteen.bc.canteen_system.exception;

public class MenuNotFoundException extends RuntimeException {
    public MenuNotFoundException(Long menuId) {
        super("Menu not found with ID: " + menuId);
    }

}
