FROM postgres:alpine
ENV POSTGRES_HOST=dbhost
ENV POSTGRES_USER=dbuser
ENV POSTGRES_PASSWORD=dbpassword 
ENV POSTGRES_DB=demo
COPY recreate.sh delete.sql create.sql insert.sql ./
