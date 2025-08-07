#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER --dbname $POSTGRES_DB <<-EOSQL
  CREATE USER $SED_PERSISTENCE_DLT_BOOTH_USER WITH ENCRYPTED PASSWORD '$SED_PERSISTENCE_DLT_BOOTH_PASSWORD';
  CREATE DATABASE $SED_PERSISTENCE_DLT_BOOTH_DATABASE;
  GRANT ALL PRIVILEGES ON DATABASE $SED_PERSISTENCE_DLT_BOOTH_DATABASE TO $SED_PERSISTENCE_DLT_BOOTH_USER;
  \c $SED_PERSISTENCE_DLT_BOOTH_DATABASE $SED_PERSISTENCE_DLT_BOOTH_USER

  CREATE SCHEMA IF NOT EXISTS dlt_booth;

  CREATE TABLE IF NOT EXISTS dlt_booth.identities (
      id          BIGSERIAL PRIMARY KEY,
      eth_address TEXT NOT NULL,
      did         TEXT NOT NULL,
      fragment    TEXT NOT NULL,
      vcredential TEXT,
      UNIQUE (eth_address)
    );

  CREATE TABLE IF NOT EXISTS dlt_booth.assets (
      id              BIGSERIAL PRIMARY KEY,
      nft_address     TEXT,
      cid             TEXT NOT NULL,
      alias           TEXT NOT NULL,
      asset_path      TEXT NOT NULL,
      offering_path   TEXT NOT NULL,
      asset_hash      TEXT NOT NULL,
      offering_hash   TEXT NOT NULL,
      sign            TEXT NOT NULL,
      publisher       BIGINT NOT NULL REFERENCES dlt_booth.identities(id) ON DELETE RESTRICT,
      UNIQUE (nft_address, cid, alias)
  );

  CREATE TABLE dlt_booth.download_requests (
      nonce           TEXT PRIMARY KEY,
      -- asset_id        BIGINT NOT NULL REFERENCES dlt_booth.assets(id) ON DELETE RESTRICT,
      requester_did   TEXT NOT NULL,
      expiration      TEXT NOT NULL
      -- expiration      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS dlt_booth.addresses (
      addr_name     TEXT NOT NULL,
      evm_address   TEXT NOT NULL
  )

EOSQL
