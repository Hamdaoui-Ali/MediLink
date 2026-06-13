package com.medilink.medilink_backend.administration.dashboard.repository;

import com.medilink.medilink_backend.administration.dashboard.domain.DoctorDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorDashboardRepository extends JpaRepository<DoctorDashboard, Long> {

	long countByStatus(String status);
}
