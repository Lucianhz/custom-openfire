#!/usr/bin/env bash

ant -Dhalt.on.plugin.error=true -Dno.package=true -f build/build.xml dist.bin
docker build -f LocalDockerfile -t custom-openfire .
