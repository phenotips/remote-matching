#!/bin/sh

mvn clean install
cp standalone-patch/patch/* ../phenomecentral-standalone-gene-matcher/webapps/phenotips/WEB-INF/lib/.
