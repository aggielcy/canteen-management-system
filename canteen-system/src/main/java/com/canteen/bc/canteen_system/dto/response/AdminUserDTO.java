package com.canteen.bc.canteen_system.dto.response;

import com.canteen.bc.canteen_system.model.Role;
import com.canteen.bc.canteen_system.model.UserType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdminUserDTO {
  private Long id;
  private String schoolId;
  private String name;
  private Role role;
  private UserType userType;
}
