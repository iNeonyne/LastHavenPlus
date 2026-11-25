# syntax=docker/dockerfile:1
FROM python:3.12-slim AS build
COPY --from=ghcr.io/astral-sh/uv:0.11.16 /uv /uvx /bin/
ENV UV_PYTHON_PREFERENCE=only-system \
    UV_NO_PYTHON_DOWNLOADS=1 \
    UV_LINK_MODE=copy \
    UV_COMPILE_BYTECODE_TIMEOUT=900
WORKDIR /app
COPY pyproject.toml uv.lock ./
RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync --frozen --no-dev --no-install-project
COPY . .
RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync --frozen --no-dev

FROM python:3.12-slim AS runtime
RUN apt-get update && \
    apt-get install -y --no-install-recommends git ca-certificates && \
    rm -rf /var/lib/apt/lists/*
RUN groupadd -g 1000 app && \
    useradd -u 1000 -g 1000 -m -s /usr/sbin/nologin app
WORKDIR /app
COPY --from=build --chown=1000:1000 /app /app
ENV PATH="/app/.venv/bin:${PATH}" \
    UV_PYTHON_PREFERENCE=only-system \
    UV_NO_PYTHON_DOWNLOADS=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    HEALTH_SERVER_HOST=127.0.0.1 \
    HEALTH_SERVER_PORT=3001
USER 1000:1000
HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=20s \
    CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:3001/health', timeout=5).status==200 else 1)"
CMD ["python", "-m", "issue_agent.worker"]
