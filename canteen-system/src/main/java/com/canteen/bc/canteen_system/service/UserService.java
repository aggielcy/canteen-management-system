package com.canteen.bc.canteen_system.service;

import java.util.Optional;
import com.canteen.bc.canteen_system.dto.request.RegisterRequest;
import com.canteen.bc.canteen_system.dto.response.LoginResponse;
import com.canteen.bc.canteen_system.entity.UserEntity;

public interface UserService {

    Optional<UserEntity> getUser(String schoolId);
    void register(RegisterRequest request);
    LoginResponse login(String schoolId, String password);
    Optional<UserEntity> getUserProfile(String schoolId);
    void updateProfile(String schoolId, RegisterRequest updateRequest);
    void changePassword(String schoolId, String oldPassword, String newPassword, String confirmPassword);
}
