#!/usr/bin/env bash

mkdir -p postgres
cd postgres || exit 1

openssl req -new -newkey rsa:2048 -keyout postgres.key -out postgres.csr -subj "/CN=postgres" -nodes
cat > client_ext.cfg << 'EOF'
[ v3_req ]
extendedKeyUsage = serverAuth
EOF
openssl x509 -req -days 3650 -in postgres.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres.crt -extfile client_ext.cfg -extensions v3_req
rm postgres.csr client_ext.cfg
sudo chown root:postgres postgres.key postgres.crt
sudo chmod 600 postgres.key
sudo chmod 600 postgres.crt

openssl req -new -newkey rsa:2048 -keyout postgres-auth-service-client.key -out postgres-auth-service-client.csr -subj "/CN=auth_service" -nodes
cat > client_ext.cfg << 'EOF'
[ v3_req ]
extendedKeyUsage = clientAuth
EOF
openssl x509 -req -days 3650 -in postgres-auth-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres-auth-service-client.crt -extfile client_ext.cfg -extensions v3_req
rm postgres-auth-service-client.csr client_ext.cfg

openssl req -new -newkey rsa:2048 -keyout postgres-room-service-client.key -out postgres-room-service-client.csr -subj "/CN=room_service" -nodes
cat > client_ext.cfg << 'EOF'
[ v3_req ]
extendedKeyUsage = clientAuth
EOF
openssl x509 -req -days 3650 -in postgres-room-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres-room-service-client.crt -extfile client_ext.cfg -extensions v3_req
rm postgres-room-service-client.csr client_ext.cfg

openssl req -new -newkey rsa:2048 -keyout postgres-user-service-client.key -out postgres-user-service-client.csr -subj "/CN=user_service" -nodes
cat > client_ext.cfg << 'EOF'
[ v3_req ]
extendedKeyUsage = clientAuth
EOF
openssl x509 -req -days 3650 -in postgres-user-service-client.csr -CA ../ssl/ca.crt -CAkey ../ssl/ca.key -CAcreateserial -out postgres-user-service-client.crt -extfile client_ext.cfg -extensions v3_req
rm postgres-user-service-client.csr client_ext.cfg

cp postgres-auth-service-client.crt postgres-auth-service-client.key ../../auth-service
cp postgres-user-service-client.crt postgres-user-service-client.key ../../user-service
cp postgres-room-service-client.crt postgres-room-service-client.key ../../room-service

rm postgres-auth-service-client.crt postgres-auth-service-client.key postgres-user-service-client.crt postgres-user-service-client.key postgres-room-service-client.crt postgres-room-service-client.key