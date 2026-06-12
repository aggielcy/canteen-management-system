package com.canteen.bc.canteen_system.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.canteen.bc.canteen_system.entity.OrdStatLogEntity;
import com.canteen.bc.canteen_system.model.OrderStatus;

@Repository
public interface OrdStatLogRepository extends JpaRepository<OrdStatLogEntity, Long> {
    List<OrdStatLogEntity> findByOrderIdOrderByChangedAtAsc(Long orderId);

    List<OrdStatLogEntity> findByLiveStatus(OrderStatus status);
}
