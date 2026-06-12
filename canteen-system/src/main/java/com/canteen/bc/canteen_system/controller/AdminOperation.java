package com.canteen.bc.canteen_system.controller;

import com.canteen.bc.canteen_system.dto.response.AdminUserDTO;
import com.canteen.bc.canteen_system.dto.response.DashboardStatsDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderWindowDTO;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/admin")
public interface AdminOperation {

  @PostMapping("/set-cut-off")
  String updateLunchCutOffTime(@RequestParam LocalTime newCutOffTime);

  @GetMapping("/get-cut-off")
  LocalTime getLunchCutOffTime();

  @GetMapping("/orders")
  List<OrderRespDTO> getAllOrders();

  @GetMapping("/users")
  List<AdminUserDTO> getAllUsers();

  @GetMapping("/dashboard")
  DashboardStatsDTO getDashboardStats();

  @GetMapping("/get-order-window")
  OrderWindowDTO getOrderWindow();

  @PostMapping("/set-order-window")
  String setOrderWindow(@RequestParam LocalTime openTime, @RequestParam LocalTime closeTime);

  @DeleteMapping("/users/{id}")
  String deleteUser(@PathVariable Long id);
}
