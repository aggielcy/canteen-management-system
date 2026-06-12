package com.canteen.bc.canteen_system.controller.impl;

import com.canteen.bc.canteen_system.controller.AdminOperation;
import com.canteen.bc.canteen_system.dto.response.AdminUserDTO;
import com.canteen.bc.canteen_system.dto.response.DashboardStatsDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderWindowDTO;
import com.canteen.bc.canteen_system.service.AdminService;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController implements AdminOperation {
  @Autowired private AdminService adminService;

  @Override
  public String updateLunchCutOffTime(LocalTime newCutOffTime) {
    this.adminService.updateLunchCutOffTime(newCutOffTime);
    return newCutOffTime.toString();
  }

  @Override
  public LocalTime getLunchCutOffTime() {
    return this.adminService.getLunchCutOffTime();
  }

  @Override
  public List<OrderRespDTO> getAllOrders() {
    return adminService.getAllOrders();
  }

  @Override
  public List<AdminUserDTO> getAllUsers() {
    return adminService.getAllUsers();
  }

  @Override
  public DashboardStatsDTO getDashboardStats() {
    return adminService.getDashboardStats();
  }

  @Override
  public OrderWindowDTO getOrderWindow() {
    return adminService.getOrderWindow();
  }

  @Override
  public String setOrderWindow(LocalTime openTime, LocalTime closeTime) {
    adminService.setOrderWindow(openTime, closeTime);
    return "ok";
  }

  @Override
  public String deleteUser(Long id) {
    try {
      adminService.deleteUser(id);
      return "deleted";
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot delete user: " + e.getMessage());
    }
  }
}
