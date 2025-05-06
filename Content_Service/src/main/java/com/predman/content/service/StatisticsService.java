package com.predman.content.service;

import com.predman.content.dto.grpc.PredictionDto;

import java.util.UUID;

public interface StatisticsService {
    PredictionDto getPrediction(UUID projectId, int estimatedDays);
}
