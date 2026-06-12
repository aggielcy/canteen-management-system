package com.canteen.bc.canteen_system.service.impl;

import com.canteen.bc.canteen_system.dto.request.OrderReqDTO;
import com.canteen.bc.canteen_system.dto.request.OrderReqDTO.OrderItemDto;
import com.canteen.bc.canteen_system.dto.response.KitchenOrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrdStatLogDTO;
import com.canteen.bc.canteen_system.dto.response.OrderCancelRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderTrackingRespDTO;
import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.OrdStatLogEntity;
import com.canteen.bc.canteen_system.entity.OrderEntity;
import com.canteen.bc.canteen_system.entity.OrderItemEntity;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.mapper.DtoMapper;
import com.canteen.bc.canteen_system.mapper.EntityMapper;
import com.canteen.bc.canteen_system.model.OrderStatus;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.repository.ItemRepository;
import com.canteen.bc.canteen_system.repository.OrdStatLogRepository;
import com.canteen.bc.canteen_system.repository.OrderRepository;
import com.canteen.bc.canteen_system.repository.UserRepository;
import com.canteen.bc.canteen_system.service.OrderService;
import com.canteen.bc.canteen_system.service.WalletService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
  @Autowired private UserRepository userRepository;
  @Autowired private ItemRepository itemRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private WalletService walletService;
  @Autowired private DtoMapper dtoMapper;
  @Autowired private EntityMapper entityMapper;
  @Autowired private OrdStatLogRepository ordStatLogRepository;

  @Override
  @Transactional
  public OrderRespDTO placeOrder(Long userId, OrderReqDTO request) {
    UserEntity user =
        this.userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    OrderEntity order = this.entityMapper.map(user, request);

    List<OrderItemDto> items = request.getItems();
    for (OrderItemDto item : items) {
      ItemEntity itemEntity =
          this.itemRepository
              .findById(item.getMenuItemId())
              .orElseThrow(() -> new RuntimeException("Item not found: " + item.getMenuItemId()));

      if (itemEntity.getQuantity() != null) {
        if (itemEntity.getQuantity() < item.getQuantity()) {
          throw new RuntimeException("Insufficient stock for: " + itemEntity.getName());
        }
        itemEntity.setQuantity(itemEntity.getQuantity() - item.getQuantity());
        this.itemRepository.save(itemEntity);
      }

      OrderItemEntity orderItemEntity = OrderItemEntity.builder()
          .menuItem(itemEntity)
          .priceAtPurchase(item.getPriceAtPurchase())
          .quantity(item.getQuantity())
          .build();
      order.addItem(orderItemEntity);
    }

    OrdStatLogEntity ordStatLog = this.entityMapper.mapOrderedLog(user, request);
    order.addStatusLog(ordStatLog);

    this.orderRepository.save(order);

    if (PaymentMethod.WALLET.equals(order.getPaymentMethod())) {
      walletService.debitWallet(userId, order.getTotalAmount(), order.getId());
    } else if (PaymentMethod.CASH.equals(order.getPaymentMethod())) {
      walletService.logCashPurchase(userId, order.getTotalAmount(), order.getId());
    }

    Long orderId = order.getId();
    return this.dtoMapper.map(request, orderId);
  }

  @Override
  @Transactional
  public OrderCancelRespDTO cancelOrder(Long userId, Long orderId) {
    UserEntity user =
        this.userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    OrderEntity targetOrder =
        this.orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    OrderStatus current = targetOrder.getFinalStatus();
    if (current == OrderStatus.PREPARING
        || current == OrderStatus.READY_FOR_PICK_UP
        || current == OrderStatus.PICKED_UP
        || current == OrderStatus.CANCELLED
        || current == OrderStatus.ABANDONED) {
      return OrderCancelRespDTO.builder()
          .isSuccess(false)
          .message("Order cannot be cancelled at this stage")
          .build();
    }

    // Restore stock for tracked items
    for (OrderItemEntity orderItem : targetOrder.getItems()) {
      ItemEntity itemEntity = orderItem.getMenuItem();
      if (itemEntity.getQuantity() != null) {
        itemEntity.setQuantity(itemEntity.getQuantity() + orderItem.getQuantity());
        this.itemRepository.save(itemEntity);
      }
    }

    targetOrder.setFinalStatus(OrderStatus.CANCELLED);
    this.orderRepository.save(targetOrder);

    OrdStatLogEntity newStatLog = this.entityMapper.mapCancelledLog(user, targetOrder);
    this.ordStatLogRepository.save(newStatLog);

    if (PaymentMethod.WALLET.equals(targetOrder.getPaymentMethod())) {
      walletService.refundToWallet(orderId, userId);
    }

    return OrderCancelRespDTO.builder().isSuccess(true).message("Order cancelled").build();
  }

  public OrderTrackingRespDTO getOrderTrackingStatus(Long orderId) {
    OrderEntity targetOrder =
        this.orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    List<OrdStatLogEntity> statusLogs =
        this.ordStatLogRepository.findByOrderIdOrderByChangedAtAsc(orderId);
    return this.dtoMapper.map(targetOrder, statusLogs);
  }

  public List<OrderRespDTO> getOrderHistory(Long userId) {
    UserEntity user =
        this.userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return this.orderRepository.findAllByUser(user).stream()
        .map(order -> this.dtoMapper.map(order))
        .toList();
  }

  // ! Addison
  @Override // Updated
  public List<KitchenOrderRespDTO> getActiveKitchenTickets() {
    return this.ordStatLogRepository.findAll().stream() //
        .filter(e -> OrderStatus.ORDERED.equals(e.getLiveStatus())) //
        .map(e -> this.dtoMapper.mapKitchenOrd(e)) //
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void startPreparingOrder(Long orderId, Long actorId) {
    OrderEntity order =
        this.orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (!OrderStatus.ORDERED.equals(order.getFinalStatus())) {
      throw new IllegalArgumentException("Order must be in ORDERED status to start preparation");
    }

    UserEntity actor = this.userRepository.findById(actorId)
        .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + actorId));

    order.setFinalStatus(OrderStatus.PREPARING);
    this.orderRepository.save(order);

    OrdStatLogEntity newStatus = this.entityMapper.mapPreparingOrd(order, actor);
    this.ordStatLogRepository.save(newStatus);
  }

  @Override
  @Transactional
  public void markOrderAsReady(Long orderId, Long actorId) {
    OrderEntity order =
        this.orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (!OrderStatus.PREPARING.equals(order.getFinalStatus())) {
      throw new IllegalArgumentException("Order must be in PREPARING status.");
    }

    UserEntity actor = this.userRepository.findById(actorId)
        .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + actorId));

    order.setFinalStatus(OrderStatus.READY_FOR_PICK_UP);
    this.orderRepository.save(order);

    OrdStatLogEntity newStatus = this.entityMapper.mapMarkOrd(order, actor);
    this.ordStatLogRepository.save(newStatus);
  }

  @Override
  @Transactional
  public String completeOrderPickup(Long orderId, Long actorId) {
    OrderEntity order =
        this.orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (!OrderStatus.READY_FOR_PICK_UP.equals(order.getFinalStatus())) {
      throw new IllegalArgumentException("Order must be in READY status.");
    }

    UserEntity actor = this.userRepository.findById(actorId)
        .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + actorId));

    order.setFinalStatus(OrderStatus.PICKED_UP);
    this.orderRepository.save(order);

    OrdStatLogEntity newStatus = this.entityMapper.mapCompleteOrd(order, actor);
    this.ordStatLogRepository.save(newStatus);

    if (PaymentMethod.CASH.equals(order.getPaymentMethod())) {
      return "Please collect physical payment for this method.";
    }
    return "Order can be picked up straightaway.";
  }

  @Override
  public List<OrderRespDTO> getAllOrders() {
    return orderRepository.findAll().stream()
        .map(dtoMapper::map)
        .toList();
  }

  @Override
  @Transactional
  public void adminSetOrderStatus(Long orderId, String status, Long actorId) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    UserEntity actor = userRepository.findById(actorId)
        .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + actorId));
    OrderStatus newStatus = OrderStatus.valueOf(status);
    order.setFinalStatus(newStatus);
    orderRepository.save(order);
    OrdStatLogEntity log = OrdStatLogEntity.builder()
        .order(order)
        .liveStatus(newStatus)
        .changedAt(LocalDateTime.now())
        .changedBy(actor)
        .build();
    ordStatLogRepository.save(log);
  }

  @Override
  @Transactional
  public void rejectOrder(Long orderId, Long actorId) {
    OrderEntity order = this.orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (!OrderStatus.ORDERED.equals(order.getFinalStatus())) {
      throw new IllegalArgumentException("Only ORDERED status can be rejected");
    }

    UserEntity actor = this.userRepository.findById(actorId)
        .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + actorId));

    order.setFinalStatus(OrderStatus.REJECTED);
    this.orderRepository.save(order);

    OrdStatLogEntity log = this.entityMapper.mapRejectedOrd(order, actor);
    this.ordStatLogRepository.save(log);
  }

  @Override
  @Transactional
  public List<OrdStatLogDTO> getAllStatusLogs() {
    return ordStatLogRepository.findAll().stream()
        .sorted((a, b) -> b.getChangedAt().compareTo(a.getChangedAt()))
        .map(log -> OrdStatLogDTO.builder()
            .logId(log.getId())
            .orderId(log.getOrder().getId())
            .customerName(log.getOrder().getUser().getName())
            .customerSchoolId(log.getOrder().getUser().getSchoolId())
            .status(log.getLiveStatus().name())
            .changedAt(log.getChangedAt())
            .changedByName(log.getChangedBy() != null ? log.getChangedBy().getName() : null)
            .changedBySchoolId(log.getChangedBy() != null ? log.getChangedBy().getSchoolId() : null)
            .build())
        .toList();
  }

  @Scheduled(cron = "0 00 16 * * MON-FRI")
  @Transactional
  public void AbandonedSweep() {
    List<OrdStatLogEntity> readOrders =
        this.ordStatLogRepository.findByLiveStatus(OrderStatus.READY_FOR_PICK_UP); //

    if (readOrders.isEmpty()) {
      return;
    }
    for (OrdStatLogEntity log : readOrders) {
      log.setLiveStatus(OrderStatus.ABANDONED);
      log.setChangedAt(LocalDateTime.now());
      this.ordStatLogRepository.save(log);

      OrderEntity order = log.getOrder();

      if (order != null) {
        order.setFinalStatus(OrderStatus.ABANDONED);
        this.orderRepository.save(order);
      }
    }
  }
}
