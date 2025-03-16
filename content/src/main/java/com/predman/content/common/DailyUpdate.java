package com.predman.content.common;

import com.predman.content.entity.ProjectStatistics;
import com.predman.content.service.ProjectStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyUpdate {
    private final ProjectStatisticsService projectStatisticsService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyStatisticsUpdate() {
        List<ProjectStatistics> latestStatisticsList = projectStatisticsService.getAllLatestStatistics();
        latestStatisticsList.forEach(
                projectStatistics -> projectStatisticsService.updateStatistics(projectStatistics.getProject().getId()));
    }
}
