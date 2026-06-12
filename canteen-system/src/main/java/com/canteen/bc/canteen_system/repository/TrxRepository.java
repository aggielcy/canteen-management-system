package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.TrxEntity;
import com.canteen.bc.canteen_system.model.TrxType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrxRepository
    extends JpaRepository<TrxEntity, Long> {
  List<TrxEntity> findByUserId(Long userId);

  List<TrxEntity> findByOrderId(Long orderId);

  List<TrxEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<TrxEntity> findByTransactionType(TrxType transactionType);
}
