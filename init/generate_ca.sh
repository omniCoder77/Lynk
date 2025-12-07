#!/usr/bin/env bash

mkdir -p ssl
cd ssl || exit

openssl req -new -nodes -x509 -days 3650 -newkey rsa:2048 -keyout ca.key -out ca.crt -config ca.cnf

cat ca.crt ca.key > "ca.pem"

cp ca.crt ../auth-service
cp ca.crt ../user-service
cp ca.crt ../room-service
cp ca.crt ../cassandra-service