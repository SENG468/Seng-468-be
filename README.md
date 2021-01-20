# Seng-468-be

---

## Docker

To build and run the docker container

1. `./gradlew bootJar` This builds the jar that will be put into the docker
2. `docker build -t stocktrade .` This creates the container image. Must be run from the `Seng-468-be/` directory
3. `docker run -p {hostPort}:8080 stocktrade` The app can then be access at the host port
4. ^C (Ctrl. C) to end the program

---

## Login and Authentication

1. Create a User POST to {HOST}/stock-trade/users/sign-up {username,password,email}
2. Login POST to {HOST}/stock-trade/users/login
3. Add the Bearer token to requests as an Authorization header