package com.canteen.bc.canteen_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu_items")
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class MenuItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "menu_id", nullable = false)
  @Setter
  private MenuEntity menuEntity;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  @Setter
  private ItemEntity itemEntity;
}