#!/bin/sh
set -e

echo "Clearing dangling data plane entries..."

PGPASSWORD="$EDC_DATASOURCE_DEFAULT_PASSWORD" psql \
  --host="$EDC_DATASOURCE_DEFAULT_HOST" \
  --port="$EDC_DATASOURCE_DEFAULT_PORT" \
  --username="$EDC_DATASOURCE_DEFAULT_USER" \
  --dbname="$EDC_DATASOURCE_DEFAULT_DATABASE" \
  --command "DELETE FROM edc_data_plane WHERE destination IS NULL;"