#!/bin/bash
if [ $# -lt 2 ]; then
  echo "Syntax: encrypt.sh <password> <property>"
  exit 1
fi
mvn validate -Pencrypt -Djasypt.password=$1 -Dproperty=$2