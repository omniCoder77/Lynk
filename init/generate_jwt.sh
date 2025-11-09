#!/usr/bin/env bash
cd ..
echo "Enter password for JWT Key (default: hyg877yyhGHGYgTFt54567yhHgHG)"
read -r JWT_KEYPASSWORD
JWT_KEYPASSWORD=${JWT_KEYPASSWORD:-hyg877yyhGHGYgTFt54567yhHgHG}

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

echo "$JWT_KEYPASSWORD" > .docker_secrets/jwt_key_password