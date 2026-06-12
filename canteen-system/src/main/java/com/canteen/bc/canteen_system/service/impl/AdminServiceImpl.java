package com.canteen.bc.canteen_system.service.impl;

import com.canteen.bc.canteen_system.dto.response.AdminUserDTO;
import com.canteen.bc.canteen_system.dto.response.DashboardStatsDTO;
import com.canteen.bc.canteen_system.dto.response.OrderRespDTO;
import com.canteen.bc.canteen_system.dto.response.OrderWindowDTO;
import com.canteen.bc.canteen_system.entity.OrderEntity;
import com.canteen.bc.canteen_system.entity.SysConfigEntity;
import com.canteen.bc.canteen_system.mapper.DtoMapper;
import com.canteen.bc.canteen_system.model.DataType;
import com.canteen.bc.canteen_system.model.OrderStatus;
import com.canteen.bc.canteen_system.repository.MenuRepository;
import com.canteen.bc.canteen_system.repository.OrderRepository;
import com.canteen.bc.canteen_system.repository.SysConfigRepository;
import com.canteen.bc.canteen_system.repository.UserRepository;
import com.canteen.bc.canteen_system.service.AdminService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
  @Autowired private SysConfigRepository sysConfigRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MenuRepository menuRepository;
  @Autowired private DtoMapper dtoMapper;

  @Transactional
  @Override
  public void updateLunchCutOffTime(LocalTime newCutOffTime) {
    Objects.requireNonNull(newCutOffTime, "newCutOffTime must not be null");

    final String configKey = "LunchCutOffKey";
    final String newValue = newCutOffTime.toString();

    Optional<SysConfigEntity> oentity = sysConfigRepository.findByConfigKey(configKey);

    if (oentity.isEmpty()) {
      SysConfigEntity entity =
          SysConfigEntity.builder()
              .configKey(configKey)
              .configValue(newValue)
              .dataType(DataType.TIME)
              .description("lunch cut off time")
              .build();

      sysConfigRepository.save(entity);
      return;
    }

    if (newValue.equals(oentity.get().getConfigValue())) {
      return;
    }

    oentity.get().setConfigValue(newValue);
  }

  @Override
  public LocalTime getLunchCutOffTime() {
    Optional<SysConfigEntity> oentity = sysConfigRepository.findByConfigKey("LunchCutOffKey");
    return LocalTime.parse(oentity.get().getConfigValue());
  }

  @Transactional
  @Override
  public void setOrderWindow(LocalTime openTime, LocalTime closeTime) {
    Objects.requireNonNull(openTime, "openTime must not be null");
    Objects.requireNonNull(closeTime, "closeTime must not be null");
    saveOrUpdate("OrderWindowOpen", openTime.toString(), "Ordering window open time");
    saveOrUpdate("OrderWindowClose", closeTime.toString(), "Ordering window close time");
  }

  @Override
  public OrderWindowDTO getOrderWindow() {
    String open  = sysConfigRepository.findByConfigKey("OrderWindowOpen")
        .map(SysConfigEntity::getConfigValue).orElse(null);
    String close = sysConfigRepository.findByConfigKey("OrderWindowClose")
        .map(SysConfigEntity::getConfigValue).orElse(null);
    return OrderWindowDTO.builder().openTime(open).closeTime(close).build();
  }

  private void saveOrUpdate(String key, String value, String description) {
    Optional<SysConfigEntity> existing = sysConfigRepository.findByConfigKey(key);
    if (existing.isEmpty()) {
      sysConfigRepository.save(SysConfigEntity.builder()
          .configKey(key).configValue(value)
          .dataType(DataType.TIME).description(description).build());
    } else {
      existing.get().setConfigValue(value);
    }
  }

  @Override
  public List<OrderRespDTO> getAllOrders() {
    return orderRepository.findAll().stream()
        .map(order -> dtoMapper.map(order))
        .toList();
  }

  @Override
  public List<AdminUserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> AdminUserDTO.builder()
            .id(user.getId())
            .schoolId(user.getSchoolId())
            .name(user.getName())
            .role(user.getRole())
            .userType(user.getUserType())
            .build())
        .toList();
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  @Override
  public DashboardStatsDTO getDashboardStats() {
    List<OrderEntity> allOrders = orderRepository.findAll();

    BigDecimal totalRevenue = allOrders.stream()
        .filter(o -> OrderStatus.PICKED_UP.equals(o.getFinalStatus()))
        .map(OrderEntity::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    Map<String, Long> ordersByStatus = allOrders.stream()
        .collect(Collectors.groupingBy(
            o -> o.getFinalStatus().name(), Collectors.counting()));

    return DashboardStatsDTO.builder()
        .totalOrders(allOrders.size())
        .totalRevenue(totalRevenue)
        .totalUsers(userRepository.count())
        .activeMenus(menuRepository.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getIsActive())).count())
        .ordersByStatus(ordersByStatus)
        .build();
  }
}
