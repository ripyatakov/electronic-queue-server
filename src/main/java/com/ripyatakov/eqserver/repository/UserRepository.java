package com.ripyatakov.eqserver.repository;

import com.ripyatakov.eqserver.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {


    List<User> findByName(String name);

    User findByEmail(String email);

    User findByToken(String token);

    List<User> findByNameLike(String name);

    List<User> findByIdLike(int id);

    List<User> findByRoleLike(String role);

    User findFirstByRoleOrderByIdDesc(String role);
}
