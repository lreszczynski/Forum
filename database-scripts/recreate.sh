#!/bin/bash
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
USER='dbuser'
PASSWORD='dbpassword'
DATABASE='test'

cd $SCRIPT_DIR
PGPASSWORD=$PASSWORD psql -U $USER $DATABASE -f delete.sql -f create.sql -f insert.sql
