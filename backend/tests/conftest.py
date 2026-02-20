import os
import subprocess
import time
import socket # <--- Добавь этот импорт
import pytest
import requests


PROJECT_ROOT = os.path.abspath(os.path.dirname(__file__) + "/..")
BUILD_DIR = os.path.join(PROJECT_ROOT, "build-debug")
BINARY = os.path.join(BUILD_DIR, "tour_guide")


def wait_postgres():
    # ... (твой код wait_postgres без изменений) ...
    for _ in range(30):
        try:
            subprocess.run(
                ["docker", "inspect", "-f", "{{.State.Health.Status}}", "test_postgres_container"],
                check=True,
                stdout=subprocess.DEVNULL,
            )
            return
        except Exception:
            time.sleep(1)
    raise RuntimeError("Postgres not ready")


# НОВАЯ ВЕРСИЯ ОЖИДАНИЯ (без HTTP, просто ждет порт)
def wait_for_port(port, timeout=15):
    start_time = time.time()
    while time.time() - start_time < timeout:
        try:
            with socket.create_connection(("localhost", port), timeout=0.1):
                return True
        except (OSError, ConnectionRefusedError):
            time.sleep(0.1)
    raise RuntimeError(f"Port {port} did not open in {timeout} seconds")


@pytest.fixture(scope="session", autouse=True)
def postgres():
    # ... (твой код postgres docker up/down без изменений) ...
    subprocess.run(
        ["docker", "compose", "-f", "docker-compose.tests.yml", "up", "-d", "postgres"],
        check=True,
    )
    wait_postgres()
    yield
    subprocess.run(
        ["docker", "compose", "-f", "docker-compose.tests.yml", "down", "-v"],
        check=True,
    )


@pytest.fixture(scope="session")
def service(postgres):
    env = os.environ.copy()
    env.update({
        "POSTGRES_HOST": "localhost",
        "POSTGRES_PORT": "5432",
        "POSTGRES_DB": "test",
        "POSTGRES_USER": "test",
        "POSTGRES_PASSWORD": "test",
        "USERVER_DISABLE_STACK_USAGE_MONITOR": "1",
    })

    # Запускаем, но без capture_output, чтобы ты видел логи приложения в консоли, если тесты упадут
    proc = subprocess.Popen(
        [
            BINARY,
            "--config", "configs/static_config.yaml",
            "--config_vars", "configs/config_vars.tests.yaml"
        ],
        cwd=PROJECT_ROOT,
        env=env,
    )

    try:
        # Проверяем, не упал ли процесс сразу (например, из-за ошибки конфига)
        if proc.poll() is not None:
             raise RuntimeError(f"App died immediately with code {proc.returncode}")
# Ждем открытия порта 8000
        wait_for_port(8000)
        
        yield
    finally:
        proc.terminate()
        proc.wait(timeout=5)


@pytest.fixture
def client(service):
    return requests