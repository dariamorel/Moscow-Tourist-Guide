import os
import subprocess
import time
import pytest
import requests


PROJECT_ROOT = os.path.abspath(os.path.dirname(__file__) + "/..")
BUILD_DIR = os.path.join(PROJECT_ROOT, "build-debug")
BINARY = os.path.join(BUILD_DIR, "ya_gamification")


def wait_postgres():
    for _ in range(30):
        try:
            subprocess.run(
                ["docker", "inspect", "-f", "{{.State.Health.Status}}", "postgres"],
                check=True,
                stdout=subprocess.DEVNULL,
            )
            return
        except Exception:
            time.sleep(1)
    raise RuntimeError("Postgres not ready")


def wait_service():
    for _ in range(40):
        try:
            r = requests.get("http://localhost:8000/health", timeout=0.5)
            if r.status_code == 200:
                return
        except Exception:
            time.sleep(0.5)
    raise RuntimeError("Service not started")


@pytest.fixture(scope="session", autouse=True)
def postgres():
    subprocess.run(
        ["docker", "compose", "-f", "docker-compose.tests.yml", "up", "-d", "postgres"],
        check=True,
    )

    time.sleep(2)
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
    })

    proc = subprocess.Popen(
        [BINARY],
        cwd=PROJECT_ROOT,
        env=env,
    )

    wait_service()
    yield
    proc.terminate()
    proc.wait(timeout=5)


@pytest.fixture
def client(service):
    return requests