#!/usr/bin/env bash

mkdir -p postgres
cd postgres || exit 1

openssl req -new -newkey rsa:2048 -keyout postgres.key -out postgres.csr -config postgres.cnf -nodes
openssl x509 -req -days 3650 -in postgres.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres.crt -extfile postgres.cnf -extensions v3_req
rm postgres.csr

openssl req -new -newkey rsa:2048 -keyout postgres-auth-service-client.key -out postgres-auth-service-client.csr -config postgres-auth-service.cnf -nodes
openssl x509 -req -days 3650 -in postgres-auth-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres-auth-service-client.crt -extfile postgres-auth-service.cnf -extensions v3_req
rm postgres-auth-service-client.csr

openssl req -new -newkey rsa:2048 -keyout postgres-room-service-client.key -out postgres-room-service-client.csr -config postgres-room-service.cnf -nodes
openssl x509 -req -days 3650 -in postgres-room-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres-room-service-client.crt -extfile postgres-room-service.cnf -extensions v3_req
rm postgres-room-service-client.csr

openssl req -new -newkey rsa:2048 -keyout postgres-user-service-client.key -out postgres-user-service-client.csr -config postgres-user-service.cnf -nodes
openssl x509 -req -days 3650 -in postgres-user-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres-user-service-client.crt -extfile postgres-user-service.cnf -extensions v3_req
rm postgres-user-service-client.csr

cp postgres-auth-service-client.crt postgres-auth-service-client.key ../../auth-service
cp postgres-user-service-client.crt postgres-user-service-client.key ../../user-service
cp postgres-room-service-client.crt postgres-room-service-client.key ../../room-service

rm postgres-auth-service-client.crt postgres-auth-service-client.key postgres-user-service-client.crt postgres-user-service-client.key postgres-room-service-client.crt postgres-room-service-client.key