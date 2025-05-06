import signal
import asyncio
import asyncpg
import grpc
import numpy as np

from scipy.stats import norm
from server.generated import prediction_pb2, prediction_pb2_grpc
from neural_network.TFT import initialize_model
import os

DATABASE_USER     = os.getenv("DATABASE_USER", "admin")
DATABASE_PASSWORD = os.getenv("DATABASE_PASSWORD", "admin")
DATABASE_HOST     = os.getenv("DATABASE_HOST", "localhost")
DATABASE_PORT     = os.getenv("DATABASE_PORT", "5430")
DATABASE_NAME     = os.getenv("DATABASE_NAME", "predman-db")


class StatisticsServicer(prediction_pb2_grpc.StatisticsServiceServicer):
    def __init__(self, pool, model):
        self.pool = pool
        self.model = model

    async def Predict(self, request, context):
        async with self.pool.acquire() as conn:
            rows = await conn.fetch(
                "SELECT days_since_start, remaining_tasks, remaining_story_points,"
                " dependency_coefficient, critical_path_length, team_size, sum_experience,"
                " available_hours, external_risk_probability"
                " FROM project_statistics WHERE project_id = $1",
                request.projectId,
            )

        data = np.array([[row['days_since_start'], row['remaining_tasks'],
                          row['remaining_story_points'], row['dependency_coefficient'],
                          row['critical_path_length'], row['team_size'], row['sum_experience'],
                          row['available_hours'], row['external_risk_probability']
                          ] for row in rows], dtype=np.float32)
        data = np.expand_dims(data, axis=1)
        predicted_days, sigma = self.model.predict(data)[0]
        pdf_v = norm.pdf(request.estimatedDays, loc=predicted_days, scale=sigma)
        predicted_days = round(predicted_days)
        return prediction_pb2.PredictionReply(
            predictedDays=int(predicted_days),
            estimatedDaysCertainty=float(pdf_v),
        )


async def serve():
    # ─── STARTUP ─────────────────────────────────────────────────────────────
    pool = await asyncpg.create_pool(
        dsn=f"postgresql://{DATABASE_USER}:{DATABASE_PASSWORD}"
            f"@{DATABASE_HOST}:{DATABASE_PORT}/{DATABASE_NAME}",
        min_size=1,
        max_size=10,
    )

    model = initialize_model("/app/neural_network/checkpoints/best_model.weights.h5")
    # ─────────────────────────────────────────────────────────────────────────

    server = grpc.aio.server()
    servicer = StatisticsServicer(pool, model)
    prediction_pb2_grpc.add_StatisticsServiceServicer_to_server(servicer, server)
    server.add_insecure_port('[::]:50051')

    loop = asyncio.get_running_loop()
    stop_event = asyncio.Event()
    for sig in (signal.SIGINT, signal.SIGTERM):
        loop.add_signal_handler(sig, stop_event.set)

    await server.start()
    print("Async gRPC server listening on 50051", flush=True)

    await stop_event.wait()

    # ─── SHUTDOWN ────────────────────────────────────────────────────────────
    await server.stop(grace=None)
    await pool.close()
    # cleanup
    # ─────────────────────────────────────────────────────────────────────────


if __name__ == "__main__":
    asyncio.run(serve())
