# Setting up PostgreSQL Test Environment

## Start the PostgreSQL Docker Instance

The following command will start up a default PostgreSQL server instance on port 5432.

```bash
docker run --rm -d \
 -e 'POSTGRES_PASSWORD=password' \
 -p 5432:5432 \
 --name postgres12 \
 postgres:12-alpine
```
