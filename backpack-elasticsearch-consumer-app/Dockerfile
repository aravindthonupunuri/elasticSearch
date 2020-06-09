FROM docker.target.com/tap/alpine-openjdk11-build:latest
ENV JAVA_HEAP_PERCENT="70"
ADD backpack-elasticsearch-consumer-app/build/distributions/backpack-elasticsearch-consumer-app.tar /
ENTRYPOINT ["/backpack-elasticsearch-consumer-app/bin/backpack-elasticsearch-consumer-app"]
