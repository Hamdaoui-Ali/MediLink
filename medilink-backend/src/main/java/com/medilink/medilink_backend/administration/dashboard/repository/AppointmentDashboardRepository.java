package com.medilink.medilink_backend.administration.dashboard.repository;

import com.medilink.medilink_backend.administration.dashboard.domain.AppointmentDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentDashboardRepository extends JpaRepository<AppointmentDashboard, Long> {

	List<AppointmentDashboard> findTop5ByOrderByCreatedAtDesc();
}
