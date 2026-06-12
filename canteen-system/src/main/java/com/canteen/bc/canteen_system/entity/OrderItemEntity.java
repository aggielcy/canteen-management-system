package com.canteen.bc.canteen_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
  private BigDecimal priceAtPurchase;

  // ! Foreign keys
  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "menu_item_id", nullable = false)
  private ItemEntity menuItem;
}
