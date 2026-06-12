package com.canteen.bc.canteen_system.controller.impl;

import com.canteen.bc.canteen_system.controller.MenuOperation;
import com.canteen.bc.canteen_system.dto.request.MenuReqDTO;
import com.canteen.bc.canteen_system.dto.response.MenuRespDTO;
import com.canteen.bc.canteen_system.dto.response.MenuWithItemsDto;
import com.canteen.bc.canteen_system.entity.MenuEntity;
import com.canteen.bc.canteen_system.service.impl.MenuServiceImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController implements MenuOperation {
  @Autowired private MenuServiceImpl menuServiceImpl;

  @Override
  public List<MenuWithItemsDto> getMenus() {
    return menuServiceImpl.getMenus();
  }

  @Override
  public MenuEntity createMenu(@RequestBody MenuReqDTO menuReqDto) {
    return this.menuServiceImpl.createMenu(menuReqDto);
  }

  @Override
  public MenuRespDTO updateMenu(@PathVariable Long id, @RequestBody MenuReqDTO menuReqDto) {
    return menuServiceImpl.updateMenu(id, menuReqDto);
  }

  @Override
  public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
    menuServiceImpl.deleteMenu(id);
    return ResponseEntity.noContent().build();
  }
}
