package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.MenuItemEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MenuItemRepository
        extends JpaRepository<MenuItemEntity, Long> {

    List<MenuItemEntity> findByMenuEntity_Id(Long menuId);

    List<MenuItemEntity> findByItemEntity_Id(Long itemId);

    Optional<MenuItemEntity> findByMenuEntityIdAndItemEntityId(Long menuId,
            Long itemId);

    Optional<MenuItemEntity> deleteByItemEntity(MenuItemEntity item);

    @Modifying
    @Transactional
    @Query("DELETE FROM MenuItemEntity m WHERE m.menuEntity.id = :menuId AND m.itemEntity.id = :itemId")
    void deleteByMenuEntity_IdAndItemEntity_Id(@Param("menuId") Long menuId, @Param("itemId") Long itemId);

    void deleteByItemEntity_Id(Long itemId);
}
