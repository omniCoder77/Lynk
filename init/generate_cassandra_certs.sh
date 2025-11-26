#!/usr/bin/env bash

mkdir -p cassandra
cd cassandra || exit 1

openssl req -new -newkey rsa:2048 -keyout cassandra.key -out cassandra.csr -config cassandra.cnf -nodes
openssl x509 -req -days 3650 -in cassandra.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out cassandra.crt -extfile cassandra.cnf -extensions v3_req
rm cassandra.csr
openssl pkcs12 -export -in cassandra.crt -inkey cassandra.key -chain -CAfile ../ssl/ca.pem -name cassandra -out cassandra.p12 -password pass:cassandra
rm cassandra.key
keytool -importkeystore -deststorepass cassandra -destkeystore cassandra.keystore.pkcs12 -srckeystore cassandra.p12 -deststoretype PKCS12  -srcstoretype PKCS12 -noprompt -srcstorepass cassandra
rm cassandra.p12
keytool -import -trustcacerts -alias CARoot -file ../ssl/ca.crt -keystore cassandra.truststore.jks -storepass cassandra -noprompt
keytool -list -v -keystore cassandra.keystore.pkcs12 -storepass cassandra