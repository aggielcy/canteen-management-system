package com.canteen.bc.canteen_system.entity;

import java.time.LocalDateTime;
import com.canteen.bc.canteen_system.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "order_status_logs",
    indexes = {@Index(name = "idx_order_id", columnList = "order_id")})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdStatLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "status", nullable = false, length = 30)
  @Enumerated(EnumType.STRING)
  private OrderStatus liveStatus;

  @Setter
  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;


  // ! Foreign keys
  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by_id")
  private UserEntity changedBy;
}
