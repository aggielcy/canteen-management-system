package com.canteen.bc.canteen_system.service.impl;

import com.canteen.bc.canteen_system.dto.request.RegisterRequest;
import com.canteen.bc.canteen_system.dto.response.LoginResponse;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.mapper.DtoMapper;
import com.canteen.bc.canteen_system.mapper.EntityMapper;
import com.canteen.bc.canteen_system.model.Role;
import com.canteen.bc.canteen_system.repository.UserRepository;
import com.canteen.bc.canteen_system.service.UserService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EntityMapper entityMapper;

  @Autowired private DtoMapper dtoMapper;

  @Override
  public Optional<UserEntity> getUser(String schoolId) {
    return userRepository.findBySchoolId(schoolId);
  }

  @Override
  @Transactional
  public void register(RegisterRequest request) {
    Optional<UserEntity> existing = userRepository.findBySchoolId(request.getSchoolId());

    if (existing.isPresent()) {
      // Allow admin to convert/update an existing account to KITCHEN role
      if (request.getRole() == Role.KITCHEN) {
        UserEntity user = existing.get();
        user.setRole(Role.KITCHEN);
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return;
      }
      throw new IllegalArgumentException("School ID already registered.");
    }

    UserEntity userEntity = entityMapper.toEntity(request);
    String hashedPassword = passwordEncoder.encode(request.getPassword());
    userEntity.setPassword(hashedPassword);

    userRepository.save(userEntity);
  }

  @Override
  public LoginResponse login(String schoolId, String password) {
    Optional<UserEntity> oUser = userRepository.findBySchoolId(schoolId);

    if (oUser.isEmpty()) {
      return dtoMapper.toLoginResponse(null, false, "Invalid School ID or password.");
    }

    UserEntity user = oUser.get();
    if (!passwordEncoder.matches(password, user.getPassword())) {
      return dtoMapper.toLoginResponse(null, false, "Invalid School ID or password.");
    }

    return dtoMapper.toLoginResponse(user, true, "Login successful.");
  }

  @Override
  public Optional<UserEntity> getUserProfile(String schoolId) {
    return userRepository.findBySchoolId(schoolId);
  }

  @Override
  @Transactional
  public void updateProfile(String schoolId, RegisterRequest updateRequest) {
    UserEntity user =
        userRepository
            .findBySchoolId(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + schoolId));

    if (!user.getSchoolId().equals(updateRequest.getSchoolId())
        && userRepository.existsBySchoolId(updateRequest.getSchoolId())) {
      throw new IllegalArgumentException("School ID already in use.");
    }

    user.setSchoolId(updateRequest.getSchoolId());
    user.setName(updateRequest.getName());
    user.setUserType(updateRequest.getUserType());

    if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
      String hashedPassword = passwordEncoder.encode(updateRequest.getPassword());
      user.setPassword(hashedPassword);
    }

    userRepository.save(user);
  }

  @Override
  @Transactional
  public void changePassword(
      String schoolId, String oldPassword, String newPassword, String confirmPassword) {
    UserEntity user =
        userRepository
            .findBySchoolId(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new IllegalArgumentException("Incorrect old password.");
    }

    if (!newPassword.equals(confirmPassword)) {
      throw new IllegalArgumentException("New password and confirmation do not match.");
    }

    String hashedPassword = passwordEncoder.encode(newPassword);
    user.setPassword(hashedPassword);

    userRepository.save(user);
  }
}
