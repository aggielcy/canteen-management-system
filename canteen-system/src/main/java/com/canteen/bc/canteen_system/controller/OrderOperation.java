package com.canteen.bc.canteen_system.controller;

import com.canteen.bc.canteen_system.dto.request.OrderReqDTO;
import com.canteen.bc.canteen_system.dto.response.KitchenOrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrdStatLogDTO;
import com.canteen.bc.canteen_system.dto.response.OrderCancelRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderTrackingRespDTO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/orders")
public interface OrderOperation {
  @PostMapping("/order")
  OrderRespDTO placeOrder(@RequestParam Long userId, @RequestBody OrderReqDTO request);

  @PostMapping("/cancel/{orderId}")
  OrderCancelRespDTO cancelOrder(@RequestParam Long userId, @PathVariable Long orderId);

  @GetMapping("/track/{orderId}")
  OrderTrackingRespDTO getOrderTrackingStatus(@PathVariable Long orderId);

  @GetMapping("/history")
  List<OrderRespDTO> getOrderHistory(@RequestParam Long userId);

  @GetMapping("/kitchen/active")
  List<KitchenOrderRespDTO> getActiveKitchenTickets();

  @PostMapping("/kitchen/{orderId}/prepare")
  void startPreparingOrder(@PathVariable Long orderId, @RequestParam Long actorId);

  @PostMapping("/kitchen/{orderId}/ready")
  void markOrderAsReady(@PathVariable Long orderId, @RequestParam Long actorId);

  @PostMapping("/kitchen/{orderId}/complete")
  String completeOrderPickup(@PathVariable Long orderId, @RequestParam Long actorId);

  @PostMapping("/kitchen/{orderId}/reject")
  void rejectOrder(@PathVariable Long orderId, @RequestParam Long actorId);

  @GetMapping("/all")
  List<OrderRespDTO> getAllOrders();

  @PostMapping("/admin/{orderId}/status")
  void adminSetOrderStatus(@PathVariable Long orderId, @RequestParam String status, @RequestParam Long actorId);

  @GetMapping("/status-logs")
  List<OrdStatLogDTO> getAllStatusLogs();
}
