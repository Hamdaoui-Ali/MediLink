package com.medilink.medilink_backend.shared.api;

import java.util.Map;

public record ApiError(
		String code,
		String message,
		Map<String, String> details
) {
}
