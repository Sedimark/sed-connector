#!/bin/sh
set -e

echo "Clearing dangling data plane entries..."

PGPASSWORD="$EDC_DATASOURCE_DEFAULT_PASSWORD" psql \
  --host="$EDC_DATASOURCE_DEFAULT_HOST" \
  --port="$EDC_DATASOURCE_DEFAULT_PORT" \
  --username="$EDC_DATASOURCE_DEFAULT_USER" \
  --dbname="$EDC_DATASOURCE_DEFAULT_DATABASE" \
  --command "DO \$\$ BEGIN IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'edc_data_plane') THEN EXECUTE 'DELETE FROM edc_data_plane WHERE destination IS NULL;'; END IF; END \$\$;"