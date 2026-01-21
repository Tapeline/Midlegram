FROM ghcr.io/astral-sh/uv:latest AS uv
FROM python:3.13-slim
COPY --from=uv /uv /uvx /bin/


# System dependencies

RUN apt-get update && apt-get install -y \
    wget ffmpeg \
    && rm -rf /var/lib/apt/lists/*

RUN wget http://nz2.archive.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2_amd64.deb

RUN dpkg -i libssl1.1_1.1.1f-1ubuntu2_amd64.deb  # For some reason is required for tdlib

RUN apt-get update && apt-get install -y git

# Build settings

WORKDIR /app

ENV PYTHONUNBUFFERED=1
ENV UV_COMPILE_BYTECODE=1
ENV UV_LINK_MODE=copy


# Dependencies (so they are cached)

COPY pyproject.toml uv.lock ./
RUN uv sync --frozen --no-install-project


# Set up whole project

COPY . .
RUN uv sync --frozen

RUN mkdir .store


# Run

CMD ["uv", "run", "uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8999"]
