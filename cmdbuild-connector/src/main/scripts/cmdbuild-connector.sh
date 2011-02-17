#!/bin/sh

DIRNAME=`dirname $0`

ROOT_DIR="${DIRNAME}/.."
BIN_DIR="${ROOT_DIR}/bin"
CONFIG_DIR="${ROOT_DIR}/config"
LIB_DIR="${ROOT_DIR}/lib"

MAINCLASS="org.cmdbuild.connector.Connector"

java \
	-cp "${LIB_DIR}/*" \
	-Dorg.cmdbuild.connector.conf.path="${CONFIG_DIR}" \
	-Dlog4j.configuration="${CONFIG_DIR}" \
	${MAINCLASS}