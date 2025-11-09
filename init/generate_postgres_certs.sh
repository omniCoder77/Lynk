#!/usr/bin/env bash

FILE_PATH="../ssl"
PG_CREDS_DIR="postgres-creds"
CA_FILE_PATH="$FILE_PATH/ca.crt"
CA_KEY_PATH="$FILE_PATH/ca.key"

PG_CNF_PATH="$FILE_PATH/$PG_CREDS_DIR/postgres.cnf"
SERVER_KEY="$FILE_PATH/$PG_CREDS_DIR/server.key"
SERVER_CSR="$FILE_PATH/$PG_CREDS_DIR/server.csr"
SERVER_CERT="$FILE_PATH/$PG_CREDS_DIR/server.crt"

if [ ! -f "$CA_FILE_PATH" ] || [ ! -f "$CA_KEY_PATH" ]; then
    echo "❌ Error: CA files ($CA_FILE_PATH and $CA_KEY_PATH) not found."
    echo "   Please run 'generate_ca.sh' first."
    exit 1
fi

mkdir -p "$FILE_PATH/$PG_CREDS_DIR"

tee "$PG_CNF_PATH" << EOF >/dev/null
[ req ]
default_bits        = 2048
default_md          = sha256
prompt              = no
distinguished_name  = req_distinguished_name
req_extensions      = v3_req

[ req_distinguished_name ]
countryName             = IN
stateOrProvinceName     = Uttar Pradesh
localityName            = Mathura
organizationName        = Ethyllium
organizationalUnitName  = PostgreSQL-Server
commonName              = postgres # Matches the Docker container/service name

[ v3_req ]
keyUsage                = digitalSignature, keyEncipherment
extendedKeyUsage        = serverAuth, clientAuth
subjectAltName          = @alt_names

[ alt_names ]
DNS.1 = postgres        # Docker container/service name
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF

openssl req -new \
    -newkey rsa:2048 \
    -keyout "$SERVER_KEY" \
    -out "$SERVER_CSR" \
    -config "$PG_CNF_PATH" \
    -nodes

openssl x509 -req \
    -days 3650 \
    -in "$SERVER_CSR" \
    -CA "$CA_FILE_PATH" \
    -CAkey "$CA_KEY_PATH" \
    -CAcreateserial \
    -out "$SERVER_CERT" \
    -extfile "$PG_CNF_PATH" \
    -extensions v3_req

chmod 600 "$SERVER_KEY"