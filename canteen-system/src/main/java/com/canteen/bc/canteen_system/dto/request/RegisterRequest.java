package com.canteen.bc.canteen_system.dto.request;

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
public class RegisterRequest {
    private String schoolId;
    private String name;
    private String password;
    private UserType userType;
    private Role role;
}
