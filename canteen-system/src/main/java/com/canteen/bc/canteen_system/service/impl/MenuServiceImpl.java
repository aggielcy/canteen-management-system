package com.canteen.bc.canteen_system.service.impl;

import com.canteen.bc.canteen_system.dto.request.MenuReqDTO;
import com.canteen.bc.canteen_system.dto.response.ItemDTO;
import com.canteen.bc.canteen_system.dto.response.MenuRespDTO;
import com.canteen.bc.canteen_system.dto.response.MenuWithItemsDto;
import com.canteen.bc.canteen_system.entity.ItemEntity;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import com.canteen.bc.canteen_system.entity.MenuItemEntity;
import com.canteen.bc.canteen_system.repository.ItemRepository;
import com.canteen.bc.canteen_system.repository.MenuItemRepository;
import com.canteen.bc.canteen_system.repository.MenuRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuServiceImpl {

  @Autowired private ItemRepository itemRepository;
  @Autowired private MenuRepository menuRepository;
  @Autowired private MenuItemRepository menuItemRepository;

  public List<MenuWithItemsDto> getMenus() {
    return menuRepository.findAll().stream().map(menu -> {
      List<ItemDTO> items = menuItemRepository.findByMenuEntity_Id(menu.getId()).stream()
          .map(MenuItemEntity::getItemEntity)
          .filter(item -> Boolean.TRUE.equals(item.getIsVisible()))
          .map(item -> ItemDTO.builder()
              .itemId(item.getId())
              .name(item.getName())
              .description(item.getDescription())
              .price(item.getPrice())
              .imageUrl(item.getImageUrl())
              .isVisible(item.getIsVisible())
              .quantity(item.getQuantity())
              .build())
          .collect(Collectors.toList());
      return MenuWithItemsDto.builder()
          .menuId(menu.getId())
          .menuName(menu.getName())
          .isActive(menu.getIsActive())
          .items(items)
          .build();
    }).collect(Collectors.toList());
  }

  public MenuEntity createMenu(MenuReqDTO dto) {
    Objects.requireNonNull(dto, "dto must not be null");

    Optional<MenuEntity> omenuEntity = this.menuRepository.findByName(dto.getName());
    if (omenuEntity.isPresent()) {
      throw new IllegalArgumentException("Menu name already exists: " + dto.getName());
    }

    MenuEntity menuEntity =
        MenuEntity.builder().name(dto.getName()).isActive(dto.getIsActive()).build();

    MenuEntity savedMenu = this.menuRepository.save(menuEntity);

    List<MenuReqDTO.Item> items = dto.getItems();

    if (items != null && !items.isEmpty()) {
      for (MenuReqDTO.Item item : items) {

        ItemEntity itemEntity =
            this.itemRepository
                .findById(item.getItemId())
                .orElseThrow(
                    () -> new IllegalArgumentException("Item not found: " + item.getItemId()));

        MenuItemEntity menuItemEntity =
            MenuItemEntity.builder() //
                .menuEntity(savedMenu) //
                .itemEntity(itemEntity) //
                .build();

        this.menuItemRepository.save(menuItemEntity);
      }
    }
    return savedMenu;
  }

  public void deleteMenu(Long id) {
    MenuEntity menu = this.menuRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
    this.menuItemRepository.findByMenuEntity_Id(id)
        .forEach(link -> this.menuItemRepository.deleteById(link.getId()));
    this.menuRepository.delete(menu);
  }

  public MenuRespDTO updateMenu(Long id, MenuReqDTO dto) {
    MenuEntity menu =
        this.menuRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));

    if (dto.getName() != null) menu.setName(dto.getName());
    if (dto.getIsActive() != null) menu.setIsActive(dto.getIsActive());

    MenuEntity saved = this.menuRepository.save(menu);

    return MenuRespDTO.builder().name(saved.getName()).isActive(saved.getIsActive()).build();
  }
}

  //   public MenuEntity getActiveMenu(){
  //   return menuRepository.findByMenuDate(LocalDate.now())
  //   .orElseThrow();
