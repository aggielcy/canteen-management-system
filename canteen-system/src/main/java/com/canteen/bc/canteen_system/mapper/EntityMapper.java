package com.canteen.bc.canteen_system.mapper;

import com.canteen.bc.canteen_system.dto.request.ItemReqDTO;
import com.canteen.bc.canteen_system.dto.request.OrderReqDTO;
import com.canteen.bc.canteen_system.dto.request.RegisterRequest;
import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import com.canteen.bc.canteen_system.entity.MenuItemEntity;
import com.canteen.bc.canteen_system.entity.OrdStatLogEntity;
import com.canteen.bc.canteen_system.entity.OrderEntity;
import com.canteen.bc.canteen_system.entity.OrderItemEntity;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.model.OrderStatus;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.model.PaymentStatus;
import com.canteen.bc.canteen_system.model.Role;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {
  /** UserEntity */
  public UserEntity toEntity(RegisterRequest request) {
    if (request == null) return null;
    return UserEntity.builder()
        .schoolId(request.getSchoolId())
        .name(request.getName())
        .password(request.getPassword())
        .role(request.getRole() != null ? request.getRole() : Role.CUSTOMER)
        .userType(request.getUserType())
        .build();
  }

  /** OrderEntity */
  public OrderEntity map(UserEntity user, OrderReqDTO request) {
    PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod());
    PaymentStatus status = method == PaymentMethod.CASH ? PaymentStatus.UNPAID : PaymentStatus.PAID;
    return OrderEntity.builder()
        .user(user)
        .finalStatus(OrderStatus.ORDERED)
        .paymentMethod(method)
        .paymentStatus(status)
        .orderTime(LocalDateTime.now())
        .totalAmount(request.getTotalAmount())
        .remark(request.getRemark())
        .build();
  }

  public OrderItemEntity map(OrderReqDTO.OrderItemDto item, MenuItemEntity menuItem) {
    return OrderItemEntity.builder()
        .menuItem(menuItem.getItemEntity())
        .priceAtPurchase(item.getPriceAtPurchase())
        .quantity(item.getQuantity())
        .build();
  }

  public OrdStatLogEntity mapOrderedLog(UserEntity user, OrderReqDTO request) {
    return OrdStatLogEntity.builder()
        .liveStatus(OrderStatus.ORDERED)
        .changedAt(LocalDateTime.now())
        .changedBy(user)
        .build();
  }

  public OrdStatLogEntity mapCancelledLog(UserEntity user, OrderEntity targetOrder) {
    return OrdStatLogEntity.builder()
        .liveStatus(OrderStatus.CANCELLED)
        .changedAt(LocalDateTime.now())
        .changedBy(user)
        .order(targetOrder)
        .build();
  }

  public OrdStatLogEntity mapPreparingOrd(OrderEntity order, UserEntity actor) {
    return OrdStatLogEntity.builder()
        .order(order)
        .liveStatus(OrderStatus.PREPARING)
        .changedAt(LocalDateTime.now())
        .changedBy(actor)
        .build();
  }

  public OrdStatLogEntity mapMarkOrd(OrderEntity order, UserEntity actor) {
    return OrdStatLogEntity.builder()
        .order(order)
        .liveStatus(OrderStatus.READY_FOR_PICK_UP)
        .changedAt(LocalDateTime.now())
        .changedBy(actor)
        .build();
  }

  public OrdStatLogEntity mapCompleteOrd(OrderEntity order, UserEntity actor) {
    return OrdStatLogEntity.builder()
        .order(order)
        .liveStatus(OrderStatus.PICKED_UP)
        .changedAt(LocalDateTime.now())
        .changedBy(actor)
        .build();
  }

  public ItemEntity map(ItemReqDTO dto) {
    return ItemEntity.builder()
        .name(dto.getName())
        .description(dto.getDescription())
        .price(dto.getPrice())
        .imageUrl(dto.getImageUrl())
        .isVisible(dto.getIsVisible())
        .quantity(dto.getQuantity())
        .build();
  }

  public MenuItemEntity map(MenuEntity menu, ItemEntity item) {
    return MenuItemEntity.builder().menuEntity(menu).itemEntity(item).build();
  }

  public OrdStatLogEntity mapRejectedOrd(OrderEntity order, UserEntity actor) {
    return OrdStatLogEntity.builder()
        .order(order)
        .liveStatus(OrderStatus.REJECTED)
        .changedAt(LocalDateTime.now())
        .changedBy(actor)
        .build();
  }
}
