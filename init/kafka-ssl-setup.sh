#!/usr/bin/env bash

FILE_PATH="ssl"
BROKER_CREDS_DIR="kafka-creds"
PASSWORD="kafka-setup"
CA_CNF_PATH="$FILE_PATH/ca.cnf"
CA_FILE_PATH="$FILE_PATH/ca.crt"
CA_KEY_PATH="$FILE_PATH/ca.key"

echo "Using default password: **$PASSWORD**"
echo "--- Certificate Setup (Non-Interactive) ---"

echo "Creating directories..."
mkdir -p "$FILE_PATH"
mkdir -p "$FILE_PATH/$BROKER_CREDS_DIR"

if [ -f "$CA_FILE_PATH" ] && [ -f "$CA_KEY_PATH" ]; then
    echo "✅ Existing CA certificate and key found. Skipping CA generation."
    SKIP_CA_GENERATION=true
else
    echo "➡️ CA certificate or key not found. Proceeding with self-signed CA generation."
    SKIP_CA_GENERATION=false
fi

if [ "$SKIP_CA_GENERATION" = false ]; then
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
organizationalUnitName  = Development
commonName              = lynk
emailAddress            = lynk@ethyllium.com

[ v3_ca ]
keyUsage                = critical, digitalSignature, cRLSign, keyCertSign
basicConstraints        = critical, CA:true
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid:always, issuer:always
EOF
    else
        echo "✅ Using existing $CA_CNF_PATH."
    fi

    echo "1. Generating self-signed CA certificate..."
    openssl req -new -nodes \
        -x509 \
        -days 365 \
        -newkey rsa:2048 \
        -keyout "$CA_KEY_PATH" \
        -out "$CA_FILE_PATH" \
        -config "$CA_CNF_PATH"

else
    echo "1. Using existing CA files."
fi

cat "$CA_FILE_PATH" "$CA_KEY_PATH" > "$FILE_PATH/ca.pem"


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
    -CAfile "$FILE_PATH/ca.pem" \
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