package com.medilink.medilink_backend.notification.repository;

import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByAppointmentIdAndType(Long appointmentId, NotificationType type);

	boolean existsByAppointmentIdAndType(Long appointmentId, NotificationType type);
}
