package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.OrderEntity;
import com.canteen.bc.canteen_system.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  List<OrderEntity> findByUserId(Long userId);

  List<OrderEntity> findAllByUser(UserEntity user);
}
