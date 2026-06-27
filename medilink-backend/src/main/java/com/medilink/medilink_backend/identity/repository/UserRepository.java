package com.medilink.medilink_backend.identity.repository;

import com.medilink.medilink_backend.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	Optional<User> findByEmailIgnoreCase(String email);
}
