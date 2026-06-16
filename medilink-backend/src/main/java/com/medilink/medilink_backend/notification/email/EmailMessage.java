package com.medilink.medilink_backend.notification.email;

public record EmailMessage(
		String to,
		String subject,
		String body
) {}
