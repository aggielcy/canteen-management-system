package com.canteen.bc.canteen_system.controller.impl;

import com.canteen.bc.canteen_system.controller.OrderOperation;
import com.canteen.bc.canteen_system.dto.request.OrderReqDTO;
import com.canteen.bc.canteen_system.dto.response.KitchenOrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrdStatLogDTO;
import com.canteen.bc.canteen_system.dto.response.OrderCancelRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderTrackingRespDTO;
import com.canteen.bc.canteen_system.service.OrderService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrderOperation {
  @Autowired private OrderService orderService;

  @Override
  public OrderRespDTO placeOrder(Long userId, OrderReqDTO request) {
    return this.orderService.placeOrder(userId, request);
  }

  @Override
  public OrderCancelRespDTO cancelOrder(Long userId, Long orderId) {
    return this.orderService.cancelOrder(userId, orderId);
  }

  @Override
  public OrderTrackingRespDTO getOrderTrackingStatus(Long orderId) {
    return this.orderService.getOrderTrackingStatus(orderId);
  }

  @Override
  public List<OrderRespDTO> getOrderHistory(Long userId) {
    return this.orderService.getOrderHistory(userId);
  }

  @Override
  public List<KitchenOrderRespDTO> getActiveKitchenTickets() {
    return this.orderService.getActiveKitchenTickets();
  }

  @Override
  public void startPreparingOrder(Long orderId, Long actorId) {
    this.orderService.startPreparingOrder(orderId, actorId);
  }

  @Override
  public void markOrderAsReady(Long orderId, Long actorId) {
    this.orderService.markOrderAsReady(orderId, actorId);
  }

  @Override
  public String completeOrderPickup(Long orderId, Long actorId) {
    return this.orderService.completeOrderPickup(orderId, actorId);
  }

  @Override
  public void rejectOrder(Long orderId, Long actorId) {
    this.orderService.rejectOrder(orderId, actorId);
  }

  @Override
  public List<OrderRespDTO> getAllOrders() {
    return this.orderService.getAllOrders();
  }

  @Override
  public void adminSetOrderStatus(Long orderId, String status, Long actorId) {
    this.orderService.adminSetOrderStatus(orderId, status, actorId);
  }

  @Override
  public List<OrdStatLogDTO> getAllStatusLogs() {
    return this.orderService.getAllStatusLogs();
  }
}
