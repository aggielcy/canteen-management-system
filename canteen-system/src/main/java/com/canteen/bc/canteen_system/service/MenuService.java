package com.canteen.bc.canteen_system.service;

import com.canteen.bc.canteen_system.dto.response.ActiveMenusWithItemsResp;

public interface MenuService {

  // ==========================================
  // CUSTOMER FACING METHODS
  // ==========================================

  /**
   * Fetches items for the daily menu. Must filter out items flagged as invisible by the kitchen and
   * limit total results to the admin-defined display limit.
   *
   * @return A restricted list of available lunch sets featuring 1 photo per item.
   */
//  List<ItemDTO> getActiveLunchMenu();

  // ==========================================
  // ADMIN / KITCHEN FACING METHODS
  // ==========================================

  /**
   * Adds a completely new menu item to the database.
   *
   * @param request DTO containing name, price, description, and exactly 1 photo URL.
   * @return The created MenuItemDTO.
   */
//  ItemDTO createMenuItem(ItemReqDTO request);

  /**
   * Modifies item text, photo, or price with immediate real-time effect on frontend.
   *
   * @param itemId The target item ID.
   * @param request DTO containing updated menu values.
   * @return The updated MenuItemDTO.
   */
//  ItemDTO updateMenuItem(Long itemId, ItemReqDTO request);

  /**
   * Preventative Inventory Buffer: Allows kitchen staff to manually hide a menu item immediately if
   * stock is critically low before formal inventory software is added.
   *
   * @param itemId The target menu item ID.
   * @param isVisible True to show on menu, False to instantly hide from customers.
   */
  
  void toggleItemVisibility(Long itemId, boolean isVisible);

  /**
   * Allows kitchen staff to toggle the availability of a menu item immediately.
   *
   * @param itemId The target menu item ID.
   * @param isAvailable False to show "sold out" status on menu.
   */
//  void toggleItemAvailability(Long itemId, boolean isAvailable);
  ActiveMenusWithItemsResp getAllActiveMenusWithVisibleItems();
  void toggleMenuAvailability(Long menuId, boolean isActive);
}
