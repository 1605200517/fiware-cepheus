#!/bin/bash
# This script is triggered by Travis-CI when a new tag applied

set -e
set -x

# Deploy JAR to Sonatype Mave Repository
#mvn deploy -q -settings=settings.xml -DskipTests=true

# Trigger Docker Automated build
#curl -s -H "Content-Type: application/json" --data "build=true" -X POST "https://registry.hub.docker.com/u/$DOCKER_USER/fiware-cepheus/trigger/$DOCKER_KEY/"

# Publish DEB packages to bintray.com Debian repository
#curl -s -T "cepheus-cep/target/cepheus-cep_$TRAVIS_TAG_all.deb" -umarc4orange:$BINTRAY_KEY "https://api.bintray.com/content/orange-opensource/Fiware-Cepheus/Cepheus-CEP/$TRAVIS_TAG/cepheus-cep_$TRAVIS_TAG_all.deb;publish=1"
#curl -s -T "cepheus-broker/target/cepheus-broker_$TRAVIS_TAG_all.deb" -umarc4orange:$BINTRAY_KEY "https://api.bintray.com/content/orange-opensource/Fiware-Cepheus/Cepheus-Broker/$TRAVIS_TAG/cepheus-broker_$TRAVIS_TAG_all.deb;publish=1"
