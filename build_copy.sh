#!/bin/sh

mvn clean install
cp standalone-patch/patch/* ../phenomecentral-standalone-remote-matching/webapps/phenotips/WEB-INF/lib/.
