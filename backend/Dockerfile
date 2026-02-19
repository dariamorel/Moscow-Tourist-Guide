FROM ghcr.io/userver-framework/ubuntu-22.04-userver-pg:latest

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv

RUN pip3 install pytest requests