package com.predman.content.service;

import com.predman.content.dto.grpc.PredictionDto;
import com.predman.statistics.PredictionReply;
import com.predman.statistics.PredictionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.predman.statistics.StatisticsServiceGrpc;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsServiceGrpc.StatisticsServiceBlockingStub stub;
    @Override
    public PredictionDto getPrediction(UUID projectId, int estimatedDays) {
        PredictionReply predictionReply = stub.predict(PredictionRequest
                .newBuilder()
                .setEstimatedDays(estimatedDays)
                .setProjectId(projectId.toString())
                .build());
        return PredictionDto
                .builder()
                .predictedDays(predictionReply.getPredictedDays())
                .certaintyPercent(predictionReply.getEstimatedDaysCertainty())
                .build();
    }
}
