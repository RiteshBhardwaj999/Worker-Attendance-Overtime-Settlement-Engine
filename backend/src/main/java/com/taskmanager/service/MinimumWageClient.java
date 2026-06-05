package com.taskmanager.service;

import com.taskmanager.dto.response.MinimumWageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Fetches the latest statutory daily minimum wage from an external government API.
 * Falls back to a default if the API is unreachable so reporting never hard-fails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinimumWageClient {

    private static final BigDecimal DEFAULT_DAILY_MINIMUM_WAGE = new BigDecimal("423.00");

    private final RestTemplate restTemplate;

    @Value("${app.external.minimum-wage-url:http://localhost:9999/minimum-wage}")
    private String minimumWageUrl;

    public BigDecimal getDailyMinimumWage() {
        try {
            MinimumWageResponse response = restTemplate.getForObject(minimumWageUrl, MinimumWageResponse.class);
            if (response != null && response.dailyMinimumWage() != null) {
                return response.dailyMinimumWage();
            }
        } catch (Exception ex) {
            log.warn("Minimum-wage API unavailable ({}); falling back to default {}",
                    ex.getMessage(), DEFAULT_DAILY_MINIMUM_WAGE);
        }
        return DEFAULT_DAILY_MINIMUM_WAGE;
    }
}
