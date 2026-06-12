package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    Optional<WalletEntity> findByUserId(Long userId);
}
