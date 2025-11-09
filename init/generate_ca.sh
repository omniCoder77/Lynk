#!/usr/bin/env bash

FILE_PATH="../ssl"
CA_CNF_PATH="$FILE_PATH/ca.cnf"
CA_FILE_PATH="$FILE_PATH/ca.crt"
CA_KEY_PATH="$FILE_PATH/ca.key"

echo "--- 1/2: CA Generation Setup ---"

echo "Creating base directory..."
mkdir -p "$FILE_PATH"

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

openssl req -new -nodes \
    -x509 \
    -days 3650 \
    -newkey rsa:2048 \
    -keyout "$CA_KEY_PATH" \
    -out "$CA_FILE_PATH" \
    -config "$CA_CNF_PATH"

cat "$CA_FILE_PATH" "$CA_KEY_PATH" > "$FILE_PATH/ca.pem"

echo "✅ CA Generation Complete. Files: $CA_FILE_PATH, $CA_KEY_PATH"