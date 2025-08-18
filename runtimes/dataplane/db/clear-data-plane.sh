#!/bin/sh
set -e

echo "Clearing dangling data plane entries..."

PGPASSWORD="$EDC_DATASOURCE_DEFAULT_PASSWORD" psql \
  --host="$EDC_DATASOURCE_DEFAULT_HOST" \
  --port="$EDC_DATASOURCE_DEFAULT_PORT" \
  --username="$EDC_DATASOURCE_DEFAULT_USER" \
  --dbname="$EDC_DATASOURCE_DEFAULT_DATABASE" \
  --command "DO \$\$ 
    DECLARE
        rows_deleted INTEGER;
    BEGIN
        IF EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_schema = 'public' 
              AND table_name = 'edc_data_plane'
        ) THEN
            EXECUTE 'DELETE FROM edc_data_plane';
            GET DIAGNOSTICS rows_deleted = ROW_COUNT;
            RAISE NOTICE '% dangling rows deleted from edc_data_plane.', rows_deleted;
        ELSE
            RAISE NOTICE 'edc_data_plane not found or has no dangling entries. No action taken.';
        END IF;
    END 
    \$\$;"