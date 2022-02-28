#!/bin/bash
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
HOST='localhost'
DATABASE='test'
USER='dbuser'
PASSWORD='dbpassword'
if [ "$1" ]; then
    HOST=$1
fi
if [ "$2" ]; then
    DATABASE=$2
fi

cd $SCRIPT_DIR
PGPASSWORD=$PASSWORD psql -h $HOST -U $USER $DATABASE -tc "CREATE EXTENSION IF NOT EXISTS \"pgcrypto\""
PGPASSWORD=$PASSWORD psql -h $HOST -U $USER $DATABASE -f delete.sql -f create.sql -f insert.sql
