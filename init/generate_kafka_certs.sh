#!/usr/bin/env bash

PASSWORD=kafka-setup
mkdir -p kafka
cd kafka || exit 1

openssl req -new -newkey rsa:2048 -keyout kafka.key -out kafka.csr -config kafka.cnf -nodes
openssl x509 -req -days 3650 -in kafka.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out kafka.crt -extfile kafka.cnf -extensions v3_req
rm kafka.csr
openssl pkcs12 -export -in kafka.crt -inkey kafka.key -chain -CAfile ../ssl/ca.pem -name kafka -out kafka.p12 -password pass:$PASSWORD
rm kafka.crt kafka.key
keytool -importkeystore -deststorepass $PASSWORD -destkeystore kafka.keystore.pkcs12 -srckeystore kafka.p12 -deststoretype PKCS12  -srcstoretype PKCS12 -noprompt -srcstorepass $PASSWORD
rm kafka.p12
keytool -import -trustcacerts -alias CARoot -file ../ssl/ca.crt -keystore kafka.truststore.jks -storepass $PASSWORD -noprompt
keytool -list -v -keystore kafka.keystore.pkcs12 -storepass $PASSWORD