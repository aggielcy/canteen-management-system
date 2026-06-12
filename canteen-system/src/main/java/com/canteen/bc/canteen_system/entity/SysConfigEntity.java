package com.canteen.bc.canteen_system.entity;

import com.canteen.bc.canteen_system.model.DataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysConfigEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "config_key", length = 100)
  private String configKey;

  @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
  private String configValue;

  @Column(name = "datatype", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private DataType dataType;

  @Column(name = "description", nullable = false, length = 255)
  private String description;
}
