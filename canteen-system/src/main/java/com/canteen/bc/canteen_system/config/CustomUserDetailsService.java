package com.canteen.bc.canteen_system.config;

import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String schoolId) throws UsernameNotFoundException {
        UserEntity user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + schoolId));

        return new User(
                user.getSchoolId(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
