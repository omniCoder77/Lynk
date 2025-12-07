#!/usr/bin/env bash

mkdir -p cassandra
cd cassandra || exit 1

openssl req -new -newkey rsa:2048 -keyout cassandra.key -out cassandra.csr -subj "/CN=cassandra" -nodes
cat > client_ext.cfg << 'EOF'
[ v3_req ]
extendedKeyUsage = serverAuth
EOF
openssl x509 -req -days 3650 -in cassandra.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out cassandra.crt -extfile client_ext.cfg -extensions v3_req
rm cassandra.csr
openssl pkcs12 -export -in cassandra.crt -inkey cassandra.key -chain -CAfile ../ssl/ca.pem -name cassandra -out cassandra.p12 -password pass:cassandra
rm cassandra.key
keytool -importkeystore -deststorepass cassandra -destkeystore cassandra.keystore.pkcs12 -srckeystore cassandra.p12 -deststoretype PKCS12  -srcstoretype PKCS12 -noprompt -srcstorepass cassandra
rm cassandra.p12
keytool -import -trustcacerts -alias CARoot -file ../ssl/ca.crt -keystore cassandra.truststore.jks -storepass cassandra -noprompt
keytool -list -v -keystore cassandra.keystore.pkcs12 -storepass cassandra

openssl req -new -newkey rsa:2048 -keyout cassandra-message-service-client.key -out cassandra-message-service-client.csr -subj "/CN=message_service" -nodes
cat > client_ext.cfg << 'EOF'
[ v3_req ]
extendedKeyUsage = clientAuth
EOF
openssl x509 -req -days 3650 -in cassandra-message-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out cassandra-message-service-client.crt -extfile client_ext.cfg -extensions v3_req
openssl pkcs12 -export -in cassandra-message-service-client.crt -inkey cassandra-message-service-client.key -name "cassandra-client" -out cassandra-client.p12 -passout pass:cassandra
rm cassandra-message-service-client.csr client_ext.cfg

rm cassandra-message-service-client.crt cassandra-message-service-client.key
cp cassandra.truststore.jks ../../message-service
mv cassandra-client.p12 ../../message-service