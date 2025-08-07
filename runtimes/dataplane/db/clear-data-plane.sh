#!/bin/sh
set -e

echo "Clearing hanging data plane entries..."

PGPASSWORD="$SED_PERSISTENCE_CONNECTOR_PASSWORD" psql \
  --host="$SED_PERSISTENCE_CONNECTOR_HOST" \
  --port="$SED_PERSISTENCE_CONNECTOR_PORT" \
  --username="$SED_PERSISTENCE_CONNECTOR_USER" \
  --dbname="$SED_PERSISTENCE_CONNECTOR_DATABASE" \
  --command "DO \$\$ BEGIN IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'edc_data_plane') THEN EXECUTE 'DELETE FROM edc_data_plane WHERE destination IS NULL;'; END IF; END \$\$;"