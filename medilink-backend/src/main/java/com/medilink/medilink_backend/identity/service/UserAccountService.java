package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.RoleRepository;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;

	public UserAccountService(RoleRepository roleRepository, UserRepository userRepository) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public User createUser(CreateUserCommand command) {
		String email = normalizeEmail(command.email());

		assertEmailAvailable(email);

		Role role = roleRepository.findByName(command.roleName())
				.orElseThrow(() -> new RoleNotFoundException(command.roleName()));

		return userRepository.save(new User(
				role,
				command.fullName().trim(),
				email,
				command.passwordHash(),
				trimToNull(command.phoneNumber())
		));
	}

	@Transactional(readOnly = true)
	public User getByEmail(String email) {
		return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> new UserNotFoundException(email));
	}

	@Transactional
	public User activate(Long id) {
		User user = getById(id);
		user.activate();
		return user;
	}

	@Transactional
	public User deactivate(Long id) {
		User user = getById(id);
		user.deactivate();
		return user;
	}

	@Transactional
	public User disable(Long id) {
		User user = getById(id);
		user.disable();
		return user;
	}

	String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}

	private void assertEmailAvailable(String email) {
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new EmailAlreadyUsedException(email);
		}
	}

	private User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
	}
}
