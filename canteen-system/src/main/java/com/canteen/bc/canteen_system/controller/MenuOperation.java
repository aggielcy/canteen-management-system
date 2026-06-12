package com.canteen.bc.canteen_system.controller;

import com.canteen.bc.canteen_system.dto.request.MenuReqDTO;
import com.canteen.bc.canteen_system.dto.response.MenuRespDTO;
import com.canteen.bc.canteen_system.dto.response.MenuWithItemsDto;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/v1/menus")
public interface MenuOperation {
  @GetMapping
  List<MenuWithItemsDto> getMenus();

  @PostMapping(value = "/menu")
  MenuEntity createMenu(@RequestBody MenuReqDTO menuReqDto);

  @PatchMapping(value = "/menu/{id}")
  MenuRespDTO updateMenu(@PathVariable Long id, @RequestBody MenuReqDTO menuReqDto);

  @DeleteMapping(value = "/menu/{id}")
  ResponseEntity<Void> deleteMenu(@PathVariable Long id);
}
