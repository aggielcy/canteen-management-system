package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import com.canteen.bc.canteen_system.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    // List<ItemEntity> findByMenuIdAndIsVisibleTrue(Long menuId);

    Optional<ItemEntity> findById (Long itemId);

    Optional<ItemEntity> findByName(String name);

}
