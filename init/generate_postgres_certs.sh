#!/usr/bin/env bash

mkdir -p postgres
cd postgres || exit 1

openssl req -new -newkey rsa:2048 -keyout postgres.key -out postgres.csr -config postgres.cnf -nodes
openssl x509 -req -days 3650 -in postgres.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres.crt -extfile postgres.cnf -extensions v3_req
rm postgres.csr