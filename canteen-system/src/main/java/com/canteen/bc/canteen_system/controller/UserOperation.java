package com.canteen.bc.canteen_system.controller;

import com.canteen.bc.canteen_system.dto.request.ChangePasswordRequest;
import com.canteen.bc.canteen_system.dto.request.LoginRequest;
import com.canteen.bc.canteen_system.dto.request.RegisterRequest;
import com.canteen.bc.canteen_system.dto.response.ChangePasswordResponse;
import com.canteen.bc.canteen_system.dto.response.LoginResponse;
import com.canteen.bc.canteen_system.dto.response.RegisterResponse;
import com.canteen.bc.canteen_system.dto.response.UserProfileResponse;
import com.canteen.bc.canteen_system.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = "/api/v1/users")
public interface UserOperation {

  @GetMapping(value = "/{schoolId}")
  UserEntity getUser(@PathVariable String schoolId);

  @PostMapping(value = "/register")
  RegisterResponse register(@RequestBody RegisterRequest registerRequest);

  @PostMapping(value = "/login")
  LoginResponse login(@RequestBody LoginRequest loginRequest);

  @GetMapping(value = "/profile")
  UserProfileResponse getUserProfile(@RequestParam String schoolId);

  @PutMapping(value = "/{schoolId}")
  UserProfileResponse updateProfile(
      @PathVariable String schoolId, @RequestBody RegisterRequest updateRequest);

  @PatchMapping(value = "/{schoolId}/password")
  ChangePasswordResponse changePassword(
      @PathVariable String schoolId, @RequestBody ChangePasswordRequest changePasswordRequest);
}
