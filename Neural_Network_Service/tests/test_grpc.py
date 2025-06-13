import sys
import os

project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, project_root)
sys.path.insert(0, os.path.join(project_root, 'server', 'generated'))

import pytest
import asyncio
import numpy as np
from unittest.mock import AsyncMock, MagicMock
from server.generated import prediction_pb2
from server.server import StatisticsServicer
from contextlib import asynccontextmanager


@pytest.mark.asyncio
async def test_predict_returns_expected_response():
    dummy_conn = AsyncMock()
    dummy_conn.fetch.return_value = [
        {
            'days_since_start': 10,
            'remaining_tasks': 5,
            'remaining_story_points': 13,
            'dependency_coefficient': 0.4,
            'critical_path_length': 3,
            'team_size': 4,
            'sum_experience': 8,
            'available_hours': 120,
            'external_risk_probability': 0.2,
        }
    ]

    @asynccontextmanager
    async def mock_acquire():
        yield dummy_conn

    dummy_pool = MagicMock()
    dummy_pool.acquire = MagicMock(side_effect=mock_acquire)

    dummy_model = MagicMock()
    dummy_model.predict.return_value = [(42.0, 2.0)]  # mean=42, std=2

    servicer = StatisticsServicer(dummy_pool, dummy_model)

    request = prediction_pb2.PredictionRequest(
        projectId="project-123",
        estimatedDays=42
    )
    context = MagicMock()

    # Act
    response = await servicer.Predict(request, context)

    # Assert
    assert isinstance(response, prediction_pb2.PredictionReply)
    assert response.predictedDays == 42
    assert 0.19 < response.estimatedDaysCertainty < 0.21