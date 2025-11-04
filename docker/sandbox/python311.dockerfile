FROM python:3.11-slim

RUN useradd -ms /bin/bash sandbox
USER sandbox
WORKDIR /workspace
