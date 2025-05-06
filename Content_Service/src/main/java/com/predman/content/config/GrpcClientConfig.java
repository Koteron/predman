package com.predman.content.config;

import com.predman.statistics.StatisticsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel statisticsServiceChannel() {
        String grpcHost = System.getenv("GRPC_SERVER_HOST");
        String grpcPort = System.getenv("GRPC_SERVER_PORT");
        return ManagedChannelBuilder.forAddress( grpcHost == null ? "localhost" : grpcHost,
                        grpcPort == null ? 5430 : Integer.parseInt(grpcPort))
                .usePlaintext()
                .build();
    }

    @Bean
    public StatisticsServiceGrpc.StatisticsServiceStub statisticsServiceStub(ManagedChannel statisticsServiceChannel) {
        return StatisticsServiceGrpc.newStub(statisticsServiceChannel);
    }

    @Bean
    public StatisticsServiceGrpc.StatisticsServiceBlockingStub
    statisticsServiceBlockingStub(ManagedChannel statisticsServiceChannel) {
        return StatisticsServiceGrpc.newBlockingStub(statisticsServiceChannel);
    }
}