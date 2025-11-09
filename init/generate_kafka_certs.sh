#!/usr/bin/env bash

FILE_PATH="../ssl"
BROKER_CREDS_DIR="kafka-creds"
PASSWORD="kafka-setup"
CA_FILE_PATH="$FILE_PATH/ca.crt"
CA_KEY_PATH="$FILE_PATH/ca.key"
CA_PEM_PATH="$FILE_PATH/ca.pem"

if [ ! -f "$CA_FILE_PATH" ] || [ ! -f "$CA_KEY_PATH" ]; then
    echo "❌ Error: CA files ($CA_FILE_PATH and $CA_KEY_PATH) not found."
    echo "   Please run 'generate_ca.sh' first."
    exit 1
fi

mkdir -p "$FILE_PATH/$BROKER_CREDS_DIR"

tee "$FILE_PATH/$BROKER_CREDS_DIR/kafka.cnf" << EOF >/dev/null
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
organizationalUnitName  = Kafka-Server
commonName              = kafka
emailAddress            = kafka@ethyllium.com

[ v3_req ]
keyUsage                = digitalSignature, keyEncipherment
extendedKeyUsage        = serverAuth, clientAuth
subjectAltName          = @alt_names

[ alt_names ]
DNS.1 = kafka
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF

openssl req -new \
    -newkey rsa:2048 \
    -keyout "$FILE_PATH/$BROKER_CREDS_DIR/kafka.key" \
    -out "$FILE_PATH/$BROKER_CREDS_DIR/kafka.csr" \
    -config "$FILE_PATH/$BROKER_CREDS_DIR/kafka.cnf" \
    -nodes

openssl x509 -req \
    -days 3650 \
    -in "$FILE_PATH/$BROKER_CREDS_DIR/kafka.csr" \
    -CA "$CA_FILE_PATH" \
    -CAkey "$CA_KEY_PATH" \
    -CAcreateserial \
    -out "$FILE_PATH/$BROKER_CREDS_DIR/kafka.crt" \
    -extfile "$FILE_PATH/$BROKER_CREDS_DIR/kafka.cnf" \
    -extensions v3_req

openssl pkcs12 -export \
    -in "$FILE_PATH/$BROKER_CREDS_DIR/kafka.crt" \
    -inkey "$FILE_PATH/$BROKER_CREDS_DIR/kafka.key" \
    -chain \
    -CAfile "$CA_PEM_PATH" \
    -name kafka \
    -out "$FILE_PATH/$BROKER_CREDS_DIR/kafka.p12" \
    -password pass:"$PASSWORD"

keytool -importkeystore \
    -deststorepass "$PASSWORD" \
    -destkeystore "$FILE_PATH/$BROKER_CREDS_DIR/kafka.keystore.pkcs12" \
    -srckeystore "$FILE_PATH/$BROKER_CREDS_DIR/kafka.p12" \
    -deststoretype PKCS12  \
    -srcstoretype PKCS12 \
    -noprompt \
    -srcstorepass "$PASSWORD"

if [ -f "$FILE_PATH/$BROKER_CREDS_DIR/kafka.truststore.jks" ]; then
    rm "$FILE_PATH/$BROKER_CREDS_DIR/kafka.truststore.jks"
fi

keytool -import -trustcacerts \
    -alias CARoot \
    -file "$CA_FILE_PATH" \
    -keystore "$FILE_PATH/$BROKER_CREDS_DIR/kafka.truststore.jks" \
    -storepass "$PASSWORD" \
    -noprompt

keytool -list -v \
    -keystore "$FILE_PATH/$BROKER_CREDS_DIR/kafka.keystore.pkcs12" \
    -storepass "$PASSWORD"