package com.canteen.bc.canteen_system.dto.response;

import com.canteen.bc.canteen_system.model.Role;
import com.canteen.bc.canteen_system.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
  private boolean isSuccess;
  private String message;
  private Long id;
  private String schoolId;
  private String name;
  private Role role;
  private UserType userType;
}
