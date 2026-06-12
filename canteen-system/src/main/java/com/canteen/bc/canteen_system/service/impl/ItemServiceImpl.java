package com.canteen.bc.canteen_system.service.impl;

import com.canteen.bc.canteen_system.dto.request.ItemReqDTO;
import com.canteen.bc.canteen_system.dto.response.ItemRespDTO;
import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import com.canteen.bc.canteen_system.entity.MenuItemEntity;
import com.canteen.bc.canteen_system.mapper.DtoMapper;
import com.canteen.bc.canteen_system.mapper.EntityMapper;
import com.canteen.bc.canteen_system.repository.ItemRepository;
import com.canteen.bc.canteen_system.repository.MenuItemRepository;
import com.canteen.bc.canteen_system.repository.MenuRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl {
  @Autowired private DtoMapper dtoMapper;
  @Autowired private ItemRepository itemRepository;
  @Autowired private MenuRepository menuRepository;
  @Autowired private MenuItemRepository menuItemRepository;
  @Autowired private EntityMapper entityMapper;

  public ItemRespDTO createItem(ItemReqDTO dto) {

    Optional<ItemEntity> oItemEntity = this.itemRepository.findByName(dto.getName());
    if (oItemEntity.isPresent()) {
      throw new IllegalArgumentException("Item name already exists: " + dto.getName());
    }

    ItemEntity item = entityMapper.map(dto);

    this.itemRepository.save(item);

    List<MenuEntity> targetMenus = new ArrayList<>();
    if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
      for (Long menuId : dto.getMenuIds()) {
        MenuEntity menu = this.menuRepository.findById(menuId).orElse(null);
        if (menu != null) {
          targetMenus.add(menu);
        }
      }
    }

    for (MenuEntity menu : targetMenus) {
      this.menuItemRepository.save(this.entityMapper.map(menu, item));
    }

    List<ItemRespDTO.Menu> assignedMenus = new ArrayList<>();
    for (MenuEntity menu : targetMenus) {
      assignedMenus.add(ItemRespDTO.Menu.builder().name(menu.getName()).build());
    }

    return this.dtoMapper.map(item);
  }

  public List<ItemEntity> getItemsFromDB() {
    return this.itemRepository.findAll();
  }

  public List<ItemRespDTO> getItems() {
    return this.itemRepository.findAll().stream()
        .map(item -> {
          List<ItemRespDTO.Menu> menus = this.menuItemRepository.findByItemEntity_Id(item.getId())
              .stream()
              .map(mi -> ItemRespDTO.Menu.builder().name(mi.getMenuEntity().getName()).build())
              .toList();
          return ItemRespDTO.builder()
              .itemId(item.getId())
              .name(item.getName())
              .description(item.getDescription())
              .price(item.getPrice())
              .imageUrl(item.getImageUrl())
              .isVisible(item.getIsVisible())
              .quantity(item.getQuantity())
              .menus(menus)
              .build();
        })
        .toList();
  }

  public void deleteItem(Long id) {
    this.itemRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found: " + id));
    this.menuItemRepository.deleteByItemEntity_Id(id);
    this.itemRepository.deleteById(id);
  }

  @Transactional
  public void unlinkMenuItem(Long menuId, Long itemId) {
    this.menuRepository.findById(menuId)
        .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
    this.itemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
    this.menuItemRepository.deleteByMenuEntity_IdAndItemEntity_Id(menuId, itemId);
  }

  public ItemRespDTO updateItem(Long id, ItemReqDTO dto) {
    ItemEntity item =
        this.itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));

    if (dto.getName() != null) item.setName(dto.getName());
    if (dto.getDescription() != null) item.setDescription(dto.getDescription());
    if (dto.getPrice() != null) item.setPrice(dto.getPrice());
    if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());
    if (dto.getIsVisible() != null) item.setIsVisible(dto.getIsVisible());
    if (dto.getQuantity() != null) item.setQuantity(dto.getQuantity());

    ItemEntity saved = this.itemRepository.save(item);

    return this.dtoMapper.map(saved);
  }

  public void updateMenuItem(Long menuId, Long itemId) {
    MenuEntity menuEntity =
        this.menuRepository
            .findById(menuId)
            .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));

    ItemEntity itemEntity =
        this.itemRepository
            .findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

    Optional<MenuItemEntity> existing =
        this.menuItemRepository.findByMenuEntityIdAndItemEntityId(menuId, itemId);

    if (existing.isPresent()) {
      throw new IllegalArgumentException("Item " + itemId + " is already linked to menu " + menuId);
    }

    this.menuItemRepository.save(this.entityMapper.map(menuEntity, itemEntity));
  }
}
