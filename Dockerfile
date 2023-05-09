FROM imixs/open-bpmn:latest

# copy imixs-server module
#WORKDIR /usr/src/app
COPY imixs-open-bpmn.server/target/imixs-open-bpmn.server-*-glsp.jar ./open-bpmn.glsp-server/target/

ENV GLSP_SERVER_JAR=open-bpmn.glsp-server/target/imixs-open-bpmn.server-*-glsp.jar


