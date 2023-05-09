# Imixs Open BPMN

The Imixs Open-BPMN - Extension

# Docker

We provide a custom Docker image that it overwriting the GLSP Server jar file

## Build

To build the docker image run:

    $ docker build . -t imixs/imixs-open-bpmn

# Push to Docker-Hub

To push the image manually to a docker repo:

    $ docker build . -t imixs/imixs-open-bpmn:latest
    $ docker push imixs/imixs-open-bpmn:latest
