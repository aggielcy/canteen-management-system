package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.SysConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysConfigRepository
    extends JpaRepository<SysConfigEntity, Long> {
  Optional<SysConfigEntity> findByConfigKey(String configKey);

}
