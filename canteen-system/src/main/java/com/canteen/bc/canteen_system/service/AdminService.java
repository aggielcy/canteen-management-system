package com.canteen.bc.canteen_system.service;

import java.time.LocalTime;

public interface AdminService {

  /**
   * Modifies the global operational cut-off clock for lunch orders. Updates are tracked globally to
   * govern customer pre-ordering and cancellations.
   *
   * @param newCutOffTime The designated daily cutoff time (e.g., 11:00 AM).
   */
  void updateLunchCutOffTime(LocalTime newCutOffTime);

  /**
   * Pulls the current active system cut-off time rule.
   *
   * @return LocalTime object containing current configured limit.
   */
  LocalTime getLunchCutOffTime();

  void setOrderWindow(LocalTime openTime, LocalTime closeTime);

  com.canteen.bc.canteen_system.dto.response.OrderWindowDTO getOrderWindow();

  java.util.List<com.canteen.bc.canteen_system.dto.response.OrderRespDTO> getAllOrders();

  java.util.List<com.canteen.bc.canteen_system.dto.response.AdminUserDTO> getAllUsers();

  com.canteen.bc.canteen_system.dto.response.DashboardStatsDTO getDashboardStats();

  void deleteUser(Long id);
}
