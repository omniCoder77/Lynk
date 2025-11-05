#!/usr/bin/env bash
cd ..
if [ ! -f example.env ]; then
  echo "Error: copy.env not present. Kindly sync and pull the repo again." >&2
  exit 1
fi

cp example.env .env

echo "Enter password for JWT Key (default: hyg877yyhGHGYgTFt54567yhHgHG)"
read -r JWT_KEYPASSWORD
JWT_KEYPASSWORD=${JWT_KEYPASSWORD:-hyg877yyhGHGYgTFt54567yhHgHG}

echo "Enter password for SSL Keystore (default: bYGvgHJhGtR5678uujhbHgG)"
read -r KAFKA_SSL_KEYSTORE_PASSWORD
KAFKA_SSL_KEYSTORE_PASSWORD=${KAFKA_SSL_KEYSTORE_PASSWORD:-bYGvgHJhGtR5678uujhbHgG}

echo "Enter password for JWT Key (default: HGHUYGTF6r5667yyuhuhHgVG)"
read -r KAFKA_SSL_TRUSTSTORE_PASSWORD
KAFKA_SSL_TRUSTSTORE_PASSWORD=${KAFKA_SSL_TRUSTSTORE_PASSWORD:-HGHUYGTF6r5667yyuhuhHgVG}

echo "Enter password for JWT Key (default: kjnhhgYGt6789ijhhgFR)"
read -r KAFKA_SSL_KEY_PASSWORD
KAFKA_SSL_KEY_PASSWORD=${KAFKA_SSL_KEY_PASSWORD:-kjnhhgYGt6789ijhhgFR}

echo "Enter Twilio Account SID"
read -r TWILIO_ACCOUNT_SID
if [ -z "$TWILIO_ACCOUNT_SID" ]; then
    echo "Error: Twilio Account SID cannot be empty." >&2
    exit 1
fi

echo "Enter Twilio Auth Token"
read -r TWILIO_AUTH_TOKEN
if [ -z "$TWILIO_AUTH_TOKEN" ]; then
    echo "Error: Twilio Account SID cannot be empty." >&2
    exit 1
fi

echo "Enter Twilio Phone Number"
read -r TWILIO_PHONE_NUMBER
if [ -z "$TWILIO_PHONE_NUMBER" ]; then
    echo "Error: Twilio Account SID cannot be empty." >&2
    exit 1
fi

set -a
source .env
set +a

mkdir -p .docker_secrets

echo "$JWT_KEYPASSWORD" > .docker_secrets/jwt_key_password
echo "$KAFKA_SSL_TRUSTSTORE_PASSWORD" > .docker_secrets/kafka_ssl_truststore_password
echo "$KAFKA_SSL_KEYSTORE_PASSWORD" > .docker_secrets/kafka_ssl_keystore_password
echo "$KAFKA_SSL_KEY_PASSWORD" > .docker_secrets/kafka_ssl_key_password

chmod 400 .docker_secrets/*

if [ ! -f keystore.p12 ]; then
  keytool -genseckey \
                   -alias jwtKey \
                   -keyalg HmacSHA256 \
                   -keysize 256 \
                   -storetype PKCS12 \
                   -keystore keystore.p12 \
                   -storepass "$JWT_KEYPASSWORD"
fi

for dir in ./*-service/; do
  cp keystore.p12 "$dir"
done

chmod +x init/kafka-ssl-setup.sh
./init/kafka-ssl-setup.sh
cp ssl/kafka-creds/kafka.keystore.pkcs12 notification-service/
cp ssl/kafka-creds/kafka.truststore.jks notification-service/

cp ssl/kafka-creds/kafka.keystore.pkcs12 message-service/
cp ssl/kafka-creds/kafka.truststore.jks message-service/