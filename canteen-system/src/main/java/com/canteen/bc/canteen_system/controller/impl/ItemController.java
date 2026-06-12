package com.canteen.bc.canteen_system.controller.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.canteen.bc.canteen_system.dto.request.ItemReqDTO;
import com.canteen.bc.canteen_system.dto.response.ItemRespDTO;
import com.canteen.bc.canteen_system.service.impl.ItemServiceImpl;

@RestController
public class ItemController {

  @Autowired private ItemServiceImpl itemServiceImpl;

  @GetMapping(value = "/item")
  public List<ItemRespDTO> getItems() {
    return itemServiceImpl.getItems();
  }

  @PostMapping(value = "/item")
  public ItemRespDTO createItem(@RequestBody ItemReqDTO dto) {
    return itemServiceImpl.createItem(dto);
  }

  @PatchMapping(value = "/item/{id}")
  public ItemRespDTO updateItem(@PathVariable Long id, @RequestBody ItemReqDTO dto) {
    return itemServiceImpl.updateItem(id, dto);
  }

  @DeleteMapping(value = "/item/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
    itemServiceImpl.deleteItem(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/menu_item")
  public void linkMenuItem(@RequestParam Long menuId, @RequestParam Long itemId) {
    itemServiceImpl.updateMenuItem(menuId, itemId);
  }

  @DeleteMapping(value = "/menu_item")
  public ResponseEntity<Void> unlinkMenuItem(@RequestParam Long menuId, @RequestParam Long itemId) {
    itemServiceImpl.unlinkMenuItem(menuId, itemId);
    return ResponseEntity.noContent().build();
  }
}
