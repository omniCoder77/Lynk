#!/usr/bin/env bash

FILE_PATH="../ssl"
CA_CNF_PATH="$FILE_PATH/ca.cnf"
CA_FILE_PATH="$FILE_PATH/ca.crt"
CA_KEY_PATH="$FILE_PATH/ca.key"

PG_CREDS_DIR="postgres-creds"
PG_CNF_PATH="$FILE_PATH/$PG_CREDS_DIR/postgres.cnf"

mkdir -p "$FILE_PATH"
mkdir -p "$FILE_PATH/$PG_CREDS_DIR"

if [ ! -f "$CA_CNF_PATH" ]; then
    echo "Creating $CA_CNF_PATH..."
    tee "$CA_CNF_PATH" << EOF >/dev/null
[ req ]
default_bits        = 2048
default_md          = sha256
prompt              = no
distinguished_name  = req_distinguished_name
x509_extensions     = v3_ca

[ req_distinguished_name ]
countryName             = IN
stateOrProvinceName     = Uttar Pradesh
localityName            = Mathura
organizationName        = Ethyllium
organizationalUnitName  = Root-CA
commonName              = lynk
emailAddress            = lynk@ethyllium.com

[ v3_ca ]
keyUsage                = critical, digitalSignature, cRLSign, keyCertSign
basicConstraints        = critical, CA:true
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid:always, issuer:always
EOF

    openssl req -new -nodes \
        -x509 \
        -days 3650 \
        -newkey rsa:2048 \
        -keyout "$CA_KEY_PATH" \
        -out "$CA_FILE_PATH" \
        -config "$CA_CNF_PATH"
else
    echo "Using existing CA files: $CA_FILE_PATH and $CA_KEY_PATH."
fi

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
extendedKeyUsage        = serverAuth, clientAuth # Server requires serverAuth
subjectAltName          = @alt_names

[ alt_names ]
DNS.1 = postgres        # Docker container/service name
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF

SERVER_KEY="$FILE_PATH/$PG_CREDS_DIR/server.key"
SERVER_CSR="$FILE_PATH/$PG_CREDS_DIR/server.csr"

openssl req -new \
    -newkey rsa:2048 \
    -keyout "$SERVER_KEY" \
    -out "$SERVER_CSR" \
    -config "$PG_CNF_PATH" \
    -nodes

SERVER_CERT="$FILE_PATH/$PG_CREDS_DIR/server.crt"

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

echo "✅ PostgreSQL Certificates Ready in $FILE_PATH/$PG_CREDS_DIR"