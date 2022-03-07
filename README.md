[![java.yml](https://github.com/lreszczynski/Forum/actions/workflows/java.yml/badge.svg)](https://github.com/lreszczynski/Forum/actions/workflows/java.yml)
[![coverage](https://github.com/lreszczynski/Forum/blob/main/.github/badges/jacoco.svg)](https://github.com/lreszczynski/Forum/actions/workflows/java.yml)
<a href="https://codeclimate.com/github/lreszczynski/Forum/maintainability"><img src="https://api.codeclimate.com/v1/badges/9729012f6f93b09c1626/maintainability" /></a>

# Forum<!-- omit in toc -->

## Table of Contents<!-- omit in toc -->

- [Live demo](#live-demo)
- [Installation, building and running instructions](#installation-building-and-running-instructions)
  - [a) with Docker](#a-with-docker)
    - [Requirements](#requirements)
    - [Build images](#build-images)
    - [Run](#run)
    - [Tests](#tests)
  - [b) without Docker](#b-without-docker)
    - [Requirements](#requirements-1)
    - [Build and run](#build-and-run)
    - [Running tests](#running-tests)
- [Sample images from the application](#sample-images-from-the-application)

Api documentation: https://lreszczynski.github.io/forum/

Jacoco report: https://github.com/lreszczynski/forum/actions/workflows/java.yml (located in build artifacts)

<figure>
  <img src="./database-scripts/database.png" width="400" caption="e"/>
  <figcaption>Database ERD</figcaption>
</figure>

# Live demo

They may take ~30 seconds to load each due to Heroku limitations

- Frontend: https://forum-app-2-react.herokuapp.com/forum
- Backend: https://forum-app-2.herokuapp.com/swagger-ui/index.html

# Installation, building and running instructions

```
git clone https://github.com/lreszczynski/forum
cd forum/
```

## a) with Docker

### Requirements

- `docker` and `docker-compose`

### Build images

```
docker-compose build
```

### Run

```
docker-compose up
```

Go to `localhost:3000` in the web browser

### Tests

```
docker-compose -f docker-compose-test.yml build

docker-compose -f docker-compose-test.yml up \
          --abort-on-container-exit springtest \
          --exit-code-from springtest
```

## b) without Docker

### Requirements

- `java`, `postgres`, `node`

### Build and run

1. Prepare a new Postgres database
2. Create tables with initial data: run
   `./database-scripts/recreate.sh` (modify `USER`, `PASSWORD`, `DATABASE` and `HOST` variables if needed)
3. Change directory to `backend/`
4. Run `./gradlew bootRun` (modify database connection values in `backend/src/main/resources/application.yaml` if needed)
5. Change directory to `react-frontend/` in new terminal session
6. Run `npm install && npm start`
7. Go to `localhost:3000` in the web browser

### Running tests

1. Change directory to `backend/`
2. Run `./gradlew test`

# Sample images from the application

<img src="./react-frontend/images/threads-mobile.png" width="400"/>
