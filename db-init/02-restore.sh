#!/bin/sh
set -e

pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" "/docker-entrypoint-initdb.d/database.dump"
