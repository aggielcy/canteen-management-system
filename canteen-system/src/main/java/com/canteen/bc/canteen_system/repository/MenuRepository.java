package com.canteen.bc.canteen_system.repository;

import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;


@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    Optional<MenuEntity> findById (Long menuId);

    Optional<MenuEntity> findByName(String name);
    
    // List<MenuEntity> findByIsActiveTrue();

    // Optional<MenuEntity> findByMenuDate(LocalDate menuDate);
    
}
