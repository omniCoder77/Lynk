#!/usr/bin/env bash

mkdir -p redis
cd redis || exit 1

openssl req -new -newkey rsa:2048 -keyout redis.key -out redis.csr -config redis.cnf -nodes
openssl x509 -req -days 3650 -in redis.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out redis.crt -extfile redis.cnf -extensions v3_req
rm redis.csr