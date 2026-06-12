package com.canteen.bc.canteen_system.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long itemId) {
        super("Item not found with ID: " + itemId);
    }
}
