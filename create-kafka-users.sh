#!/bin/bash
set -e

bin/kafka-configs.sh --bootstrap-server localhost:9093 \
  --alter --add-config 'SCRAM-SHA-512=[password=broker_secret]' \
  --entity-type users --entity-name broker_user

bin/kafka-configs.sh --bootstrap-server localhost:9093 \
  --alter --add-config 'SCRAM-SHA-512=[password=message-service]' \
  --entity-type users --entity-name messageService

bin/kafka-configs.sh --bootstrap-server localhost:9093 \
  --alter --add-config 'SCRAM-SHA-512=[password=notification-service]' \
  --entity-type users --entity-name notificationService