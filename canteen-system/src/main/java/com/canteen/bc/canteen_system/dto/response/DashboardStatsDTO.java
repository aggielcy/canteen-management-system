package com.canteen.bc.canteen_system.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashboardStatsDTO {
  private long totalOrders;
  private long totalUsers;
  private long activeMenus;
  private BigDecimal totalRevenue;
  private Map<String, Long> ordersByStatus;
}
