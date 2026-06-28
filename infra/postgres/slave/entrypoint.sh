#!/bin/bash
set -e

if [ ! -f "$PGDATA/PG_VERSION" ]; then

  until pg_isready -h postgres-master -U orderflow
  do
    sleep 2
  done

  until PGPASSWORD=replicator pg_basebackup \
      -h postgres-master \
      -U replicator \
      -D "$PGDATA" \
      -Fp \
      -Xs \
      -R
  do
    echo "Waiting for replication user..."
    sleep 2
  done

fi

exec docker-entrypoint.sh postgres -c config_file=/etc/postgresql/postgresql.conf