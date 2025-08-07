#!/bin/sh
set -e

echo "Clearing hanging data plane entries..."

PGPASSWORD="$SED_PERSISTENCE_CONNECTOR_PASSWORD" psql \
  --host="$SED_PERSISTENCE_CONNECTOR_HOST" \
  --port="$SED_PERSISTENCE_CONNECTOR_PORT" \
  --username="$SED_PERSISTENCE_CONNECTOR_USER" \
  --dbname="$SED_PERSISTENCE_CONNECTOR_DATABASE" \
  --command "DELETE FROM edc_data_plane WHERE destination IS NULL;"