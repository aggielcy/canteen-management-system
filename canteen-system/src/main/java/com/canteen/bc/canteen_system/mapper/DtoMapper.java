package com.canteen.bc.canteen_system.mapper;

import com.canteen.bc.canteen_system.dto.WalletResponse;
import com.canteen.bc.canteen_system.dto.WalletTransactionDTO;
import com.canteen.bc.canteen_system.dto.request.OrderReqDTO;
import com.canteen.bc.canteen_system.dto.response.ItemDTO;
import com.canteen.bc.canteen_system.dto.response.ItemRespDTO;
import com.canteen.bc.canteen_system.dto.response.KitchenOrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.LoginResponse;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderTrackingRespDTO;
import com.canteen.bc.canteen_system.dto.response.UserProfileResponse;
import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.OrdStatLogEntity;
import com.canteen.bc.canteen_system.entity.OrderEntity;
import com.canteen.bc.canteen_system.entity.OrderItemEntity;
import com.canteen.bc.canteen_system.entity.TrxEntity;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.entity.WalletEntity;
import com.canteen.bc.canteen_system.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

  /**
   * Maps an OrderReqDTO to an OrderRespDTO with the given order ID.
   *
   * @param req the order request DTO containing order details
   * @param orderId the generated order ID
   * @return the order response DTO with mapped fields and ORDERED status
   */
  public OrderRespDTO map(OrderReqDTO req, Long orderId) {
    List<OrderRespDTO.OrderedItemDto> orderedItems =
        req.getItems().stream().map(item -> this.map(item)).toList();

    return OrderRespDTO.builder()
        .orderId(orderId)
        .orderTime(LocalDateTime.now())
        .totalAmount(req.getTotalAmount())
        .orderedItems(orderedItems)
        .remark(req.getRemark())
        .orderStatus(OrderStatus.ORDERED)
        .build();
  }

  /**
   * Maps an OrderItemDto from the order request to an OrderedItemDto for the response.
   *
   * @param itemDto the order item DTO containing item details
   * @return the mapped ordered item DTO for the response
   */
  private OrderRespDTO.OrderedItemDto map(OrderReqDTO.OrderItemDto itemDto) {
    return OrderRespDTO.OrderedItemDto.builder()
        .menuItemId(itemDto.getMenuItemId())
        .name(itemDto.getName())
        .priceAtPurchase(itemDto.getPriceAtPurchase())
        .quantity(itemDto.getQuantity())
        .subTotal(null)
        .build();
  }

  public OrderRespDTO map(OrderEntity order) {
    List<OrderRespDTO.OrderedItemDto> orderedItems =
        order.getItems().stream().map(item -> this.map(item)).toList();
    return OrderRespDTO.builder()
        .orderId(order.getId())
        .customerName(order.getUser() != null ? order.getUser().getName() : null)
        .schoolId(order.getUser() != null ? order.getUser().getSchoolId() : null)
        .orderTime(order.getOrderTime())
        .totalAmount(order.getTotalAmount())
        .orderedItems(orderedItems)
        .remark(order.getRemark())
        .orderStatus(order.getFinalStatus())
        .paymentMethod(order.getPaymentMethod())
        .build();
  }

  private OrderRespDTO.OrderedItemDto map(OrderItemEntity item) {
    return OrderRespDTO.OrderedItemDto.builder()
        .menuItemId(item.getMenuItem().getId())
        .name(item.getMenuItem().getName())
        .priceAtPurchase(item.getPriceAtPurchase())
        .quantity(item.getQuantity())
        .subTotal(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
        .build();
  }

  public KitchenOrderRespDTO mapKitchenOrd(OrdStatLogEntity ordStatLogEntity) {
    return KitchenOrderRespDTO.builder() //
        .order_Id(ordStatLogEntity.getId()) //
        .name(ordStatLogEntity.getOrder().getUser().getName()) //
        .liveStatus(ordStatLogEntity.getLiveStatus()) //
        .build();
  }

  public ItemDTO toDto(ItemEntity entity) {
    if (entity == null) {
      return null;
    }
    return ItemDTO.builder()
        .itemId(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .price(entity.getPrice())
        .imageUrl(entity.getImageUrl())
        .isVisible(entity.getIsVisible())
        .quantity(entity.getQuantity())
        .build();
  }

  public OrderTrackingRespDTO map(OrderEntity targetOrder, List<OrdStatLogEntity> statusLogs) {
    List<OrderTrackingRespDTO.OrderTrackingStatus> orderTrackingStatus =
        statusLogs.stream().map(log -> this.map(log)).toList();
    return OrderTrackingRespDTO.builder()
        .orderId(targetOrder.getId())
        .remark(targetOrder.getRemark())
        .orderedItems(
            targetOrder.getItems().stream().map(item -> this.mapToTrackingItem(item)).toList())
        .orderTrackingStatus(orderTrackingStatus)
        .build();
  }

  private OrderTrackingRespDTO.OrderTrackingStatus map(OrdStatLogEntity log) {
    return OrderTrackingRespDTO.OrderTrackingStatus.builder()
        .timestamp(log.getChangedAt())
        .status(log.getLiveStatus())
        .build();
  }

  private OrderTrackingRespDTO.OrderedItemDto mapToTrackingItem(OrderItemEntity item) {
    return OrderTrackingRespDTO.OrderedItemDto.builder()
        .menuItemId(item.getMenuItem().getId())
        .name(item.getMenuItem().getName())
        .quantity(item.getQuantity())
        .build();
  }

  /** UserDTO */
  public UserProfileResponse toProfileResponse(UserEntity user, boolean isSuccess, String message) {
    if (user == null) {
      return UserProfileResponse.builder().isSuccess(isSuccess).message(message).build();
    }
    return UserProfileResponse.builder()
        .isSuccess(isSuccess)
        .message(message)
        .id(user.getId())
        .schoolId(user.getSchoolId())
        .name(user.getName())
        .role(user.getRole())
        .userType(user.getUserType())
        .build();
  }

  public LoginResponse toLoginResponse(UserEntity user, boolean isSuccess, String message) {
    if (user == null) {
      return LoginResponse.builder().isSuccess(isSuccess).message(message).build();
    }
    return LoginResponse.builder()
        .isSuccess(isSuccess)
        .message(message)
        .id(user.getId())
        .schoolId(user.getSchoolId())
        .role(user.getRole())
        .userType(user.getUserType())
        .build();
  }

  /** WalletDTO */
  public WalletResponse map(WalletEntity wallet) {
    if (wallet == null) {
      return null;
    }
    return WalletResponse.builder()
        .id(wallet.getId())
        .userId(wallet.getUser().getId())
        .userName(wallet.getUser().getName())
        .balance(wallet.getBalance())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  public WalletTransactionDTO map(TrxEntity transaction, BigDecimal balanceAfter) {
    if (transaction == null) {
      return null;
    }
    return WalletTransactionDTO.builder()
        .transactionId(transaction.getId())
        .userId(transaction.getUser().getId())
        .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
        .paymentMethod(transaction.getPaymentMethod())
        .transactionType(transaction.getTransactionType())
        .status(transaction.getStatus())
        .amount(transaction.getAmount())
        .balanceAfter(balanceAfter)
        .createdAt(transaction.getCreatedAt())
        .build();
  }

  public ItemRespDTO map(ItemEntity item) {
    return ItemRespDTO.builder()
        .name(item.getName())
        .description(item.getDescription())
        .price(item.getPrice())
        .imageUrl(item.getImageUrl())
        .isVisible(item.getIsVisible())
        .quantity(item.getQuantity())
        .build();
  }
}
