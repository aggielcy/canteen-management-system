package com.canteen.bc.canteen_system.controller.impl;

import com.canteen.bc.canteen_system.controller.UserOperation;
import com.canteen.bc.canteen_system.dto.request.ChangePasswordRequest;
import com.canteen.bc.canteen_system.dto.request.LoginRequest;
import com.canteen.bc.canteen_system.dto.request.RegisterRequest;
import com.canteen.bc.canteen_system.dto.response.ChangePasswordResponse;
import com.canteen.bc.canteen_system.dto.response.LoginResponse;
import com.canteen.bc.canteen_system.dto.response.RegisterResponse;
import com.canteen.bc.canteen_system.dto.response.UserProfileResponse;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.mapper.DtoMapper;
import com.canteen.bc.canteen_system.service.UserService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserOperation {

  @Autowired private UserService userService;

  @Autowired private DtoMapper dtoMapper;

  @Override
  public UserEntity getUser(String schoolId) {
    return userService.getUser(schoolId).orElse(null);
  }

  @Override
  public RegisterResponse register(RegisterRequest registerRequest) {
    try {
      userService.register(registerRequest);
      return RegisterResponse.builder().isSuccess(true).message("Account created successfully.").build();
    } catch (IllegalArgumentException e) {
      return RegisterResponse.builder()
          .isSuccess(false)
          .message(e.getMessage())
          .build();
    } catch (Exception e) {
      return RegisterResponse.builder()
          .isSuccess(false)
          .message("Registration failed: " + e.getMessage())
          .build();
    }
  }

  @Override
  public LoginResponse login(LoginRequest loginRequest) {
    return userService.login(loginRequest.getSchoolId(), loginRequest.getPassword());
  }

  @Override
  public UserProfileResponse getUserProfile(String schoolId) {
    Optional<UserEntity> userOpt = userService.getUserProfile(schoolId);
    if (userOpt.isPresent()) {
      return dtoMapper.toProfileResponse(userOpt.get(), true, "Profile retrieved successfully.");
    }
    return dtoMapper.toProfileResponse(null, false, "Profile not found.");
  }

  @Override
  public UserProfileResponse updateProfile(String schoolId, RegisterRequest updateRequest) {
    try {
      userService.updateProfile(schoolId, updateRequest);
      Optional<UserEntity> updatedUser = userService.getUser(schoolId);
      if (updatedUser.isPresent()) {
        return dtoMapper.toProfileResponse(
            updatedUser.get(), true, "Profile updated successfully.");
      }
      return dtoMapper.toProfileResponse(null, false, "Failed to retrieve updated profile.");
    } catch (IllegalArgumentException e) {
      return dtoMapper.toProfileResponse(null, false, e.getMessage());
    }
  }

  @Override
  public ChangePasswordResponse changePassword(String schoolId, ChangePasswordRequest request) {
    try {
      userService.changePassword(
          schoolId,
          request.getOldPassword(),
          request.getNewPassword(),
          request.getConfirmNewPassword());
      return ChangePasswordResponse.builder()
          .isSuccess(true)
          .message("Password updated successfully.")
          .build();
    } catch (IllegalArgumentException e) {
      return ChangePasswordResponse.builder().isSuccess(false).message(e.getMessage()).build();
    }
  }
}
