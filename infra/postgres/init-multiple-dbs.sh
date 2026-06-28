#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<EOSQL
    CREATE DATABASE orderflow_inventory;
    CREATE DATABASE orderflow_saga;

    CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replicator';
EOSQL

echo "host replication replicator all trust" >> "$PGDATA/pg_hba.conf"