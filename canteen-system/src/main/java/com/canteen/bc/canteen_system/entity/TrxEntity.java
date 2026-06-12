package com.canteen.bc.canteen_system.entity;

import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.model.TrxStatus;
import com.canteen.bc.canteen_system.model.TrxType;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "financial_transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TrxEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TrxType transactionType;

  @Column(name = "payment_method", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Setter
  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TrxStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  // ! Foreign keys

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderEntity order;
}
