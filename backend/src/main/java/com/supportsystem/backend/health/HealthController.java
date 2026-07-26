package com.supportsystem.backend.health;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private static final ZoneId EASTERN = ZoneId.of("America/New_York");
	private static final DateTimeFormatter TIMESTAMP_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm:ss a zzz");

	@GetMapping("/api/health")
	public Map<String, Object> health() {
		return Map.of(
				"status", "UP",
				"service", "backend",
				"timestamp", ZonedDateTime.now(EASTERN).format(TIMESTAMP_FORMAT));
	}
}
