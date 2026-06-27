package com.wellnesspro.config;

import com.wellnesspro.service.RenewalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Drives {@link RenewalService} on a timer. Gated by {@code app.renewal.scheduler-enabled}
 * (default true); the test profile sets it false so the job never fires during tests and
 * the renewal logic is exercised by calling the service directly instead. {@code @EnableScheduling}
 * lives here so it too is switched off with the flag.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.renewal.scheduler-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RenewalScheduler {

    private final RenewalService renewalService;

    /** Daily at 02:00 server time: renew everything due as of today. */
    @Scheduled(cron = "0 0 2 * * *")
    public void renewDaily() {
        renewalService.renewDueSubscriptions(LocalDate.now());
    }
}
