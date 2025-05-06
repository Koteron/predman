FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
RUN python -m grpc_tools.protoc -I=server\
 --python_out=. --grpc_python_out=server/generated server/prediction.proto
EXPOSE 50051
CMD ["python", "-m", "server.server"]
