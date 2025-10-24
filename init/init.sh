#!/usr/bin/env bash

cd Lynk || (echo "Lynk directory not found, kindly cd to the downloaded directory" && exit 1)

if [ ! -f ./example.env ]; then
  echo "Error: copy.env not present. Kindly sync and pull the repo again." >&2
  exit 1
fi

cp example.env .env
echo "Add your twilio secrets to .env file"

set -a
source .env
set +a

if [ -f keystore.p12 ]; then
  rm keystore.p12
fi
keytool -genseckey \
  -alias jwtKey \
  -keyalg HmacSHA256 \
  -keysize 256 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -storepass "$JWT_KEYPASSWORD" \

for dir in ./*-service/; do
  cp keystore.p12 "$dir"
done

docker compose up