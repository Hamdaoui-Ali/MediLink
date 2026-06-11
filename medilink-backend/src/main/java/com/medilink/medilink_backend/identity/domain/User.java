package com.medilink.medilink_backend.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Column(name = "full_name", nullable = false, length = 160)
	private String fullName;

	@Column(nullable = false, unique = true, length = 190)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "phone_number", length = 40)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", nullable = false, length = 30)
	private AccountStatus accountStatus;

	protected User() {
	}

	public User(Role role, String fullName, String email, String passwordHash, String phoneNumber) {
		this.role = role;
		this.fullName = fullName;
		this.email = email;
		this.passwordHash = passwordHash;
		this.phoneNumber = phoneNumber;
		this.accountStatus = AccountStatus.ACTIVE;
	}

	public Long getId() {
		return id;
	}

	public Role getRole() {
		return role;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void updateProfile(String fullName, String email, String phoneNumber) {
		this.fullName = fullName;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	public void updatePasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void activate() {
		this.accountStatus = AccountStatus.ACTIVE;
	}

	public void deactivate() {
		this.accountStatus = AccountStatus.INACTIVE;
	}

	public void disable() {
		this.accountStatus = AccountStatus.DISABLED;
	}
}
