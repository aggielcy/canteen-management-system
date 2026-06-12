package com.canteen.bc.canteen_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.canteen.bc.canteen_system.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findBySchoolId(String schoolId);

    boolean existsBySchoolId(String schoolId);
}
