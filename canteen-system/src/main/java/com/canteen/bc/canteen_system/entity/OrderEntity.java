package com.canteen.bc.canteen_system.entity;

import com.canteen.bc.canteen_system.model.OrderStatus;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.model.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "final_status", nullable = false, length = 30)
  @Enumerated(EnumType.STRING)
  private OrderStatus finalStatus;

  @Column(name = "payment_method", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(name = "payment_status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Column(name = "order_time", nullable = false)
  private LocalDateTime orderTime;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "remark", length = 500)
  private String remark;

  // ! Foreign keys
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemEntity> items = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrdStatLogEntity> statusHistory = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TrxEntity> transactions = new ArrayList<>();

  // methods
  public void addItem(OrderItemEntity item) {
    if (items == null) {
      items = new ArrayList<>();
    }
    items.add(item);
    item.setOrder(this);
  }

  public void addStatusLog(OrdStatLogEntity log) {
    if (statusHistory == null) {
      statusHistory = new ArrayList<>();
    }
    statusHistory.add(log);
    log.setOrder(this);
  }

  public void addTransaction(TrxEntity transaction) {
    if (transactions == null) {
      transactions = new ArrayList<>();
    }
    transactions.add(transaction);
    transaction.setOrder(this);
  }
}
