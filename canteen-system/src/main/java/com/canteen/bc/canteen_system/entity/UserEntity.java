package com.canteen.bc.canteen_system.entity;

import com.canteen.bc.canteen_system.model.Role;
import com.canteen.bc.canteen_system.model.UserType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "school_id", nullable = false, unique = true, length = 50)
  private String schoolId;

  @Setter
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Setter
  @Column(name = "password", nullable = false, length = 100)
  private String password;

  @Setter
  @Column(name = "role", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Setter
  @Column(name = "user_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private UserType userType;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private WalletEntity wallet;
}
