package com.predman.content.dto.grpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record PredictionDto(
    @NonNull
    @JsonProperty(value = "certainty_percent")
    Double certaintyPercent,

    @NonNull
    @JsonProperty(value = "predicted_deadline")
    Integer predictedDays
) {
}
