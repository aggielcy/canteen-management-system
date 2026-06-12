package com.canteen.bc.canteen_system.service;

import com.canteen.bc.canteen_system.dto.request.OrderReqDTO;
import com.canteen.bc.canteen_system.dto.response.KitchenOrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrdStatLogDTO;
import com.canteen.bc.canteen_system.dto.response.OrderCancelRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderTrackingRespDTO;
import java.util.List;

public interface OrderService {

  // ==========================================
  // CUSTOMER ACTIONS
  // ==========================================

  /**
   * Handles order checkouts. Validates time against the admin cut-off if it's a pre-order. Pushes
   * order to state 'ORDERED'. Automatically logs 'order_time' timestamp.
   *
   * @param userId ID of checking out user (= school ID).
   * @param request Contains selected items, quantities, and chosen PaymentType (WALLET vs CASH).
   * @return OrderResponseDTO containing order ID, total price, and tracking status.
   */
  OrderRespDTO placeOrder(Long userId, OrderReqDTO request);

  /**
   * Allows a user to cancel their order. CRITICAL CHECK: Implementation must verify current time is
   * BEFORE the admin cut-off time. If true, changes state to 'CANCELLED'. Wallet balances are NOT
   * auto-refunded to the app (meeting rule: cash manual refund at counter only).
   *
   * @param userId Requesting customer ID.
   * @param orderId Target order ID to terminate.
   */
  OrderCancelRespDTO cancelOrder(Long userId, Long orderId);

  /**
   * Live UI Tracking Feed: Pulls the singular state of an order for live web updates.
   *
   * @param orderId Target order ID.
   * @return OrderTrackingDTO containing order identification and OrderStatus enum value.
   */
  OrderTrackingRespDTO getOrderTrackingStatus(Long orderId);

  List<OrderRespDTO> getOrderHistory(Long userId);

  // ==========================================
  // KITCHEN & COUNTER ACTIONS (State Machine)
  // ==========================================

  /**
   * Kitchen Dashboard Query: Pulls all orders currently pending preparation.
   *
   * @return List of active orders in 'ORDERED' or 'PREPARING' status.
   */
  List<KitchenOrderRespDTO> getActiveKitchenTickets();

  /**
   * Quick-Tap Action 1: Transitions state from 'ORDERED' to 'PREPARING'. Moves ticket into the
   * kitchen's active working queue.
   *
   * @param orderId Target order ID.
   */
  void startPreparingOrder(Long orderId, Long actorId);

  /**
   * Quick-Tap Action 2: Transitions state from 'PREPARING' to 'READY_FOR_PICK_UP'. Automatically
   * captures the 'ready_for_pickup_time' millisecond timestamp.
   *
   * @param orderId Target order ID.
   * @param actorId ID of the kitchen staff performing the action.
   */
  void markOrderAsReady(Long orderId, Long actorId);

  /**
   * Handover Action: Transitions state from 'READY_FOR_PICK_UP' to 'PICKED_UP'. CRITICAL CHECK: If
   * transaction was marked 'CASH', system must prompt counter operator to collect physical cash
   * before completing transaction.
   *
   * @param orderId Target order ID.
   * @param actorId ID of the kitchen staff performing the action.
   */
  String completeOrderPickup(Long orderId, Long actorId); // ! changed return type to String

  // ==========================================
  // AUTOMATED BACKGROUND UTILITIES
  // ==========================================

  /**
   * Automated End-of-Day Sweep: Cron expression utility executing at canteen closing. Finds all
   * unclaimed records remaining in 'READY_FOR_PICK_UP' and moves them cleanly to 'ABANDONED' to
   * refresh queues for the next morning.
   */
  void AbandonedSweep();

  void rejectOrder(Long orderId, Long actorId);

  // ==========================================
  // ADMIN ACTIONS
  // ==========================================

  List<OrderRespDTO> getAllOrders();

  void adminSetOrderStatus(Long orderId, String status, Long actorId);

  List<OrdStatLogDTO> getAllStatusLogs();
}
