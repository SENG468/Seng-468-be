# Seng-468-be

---

## Running the Application

1. Add all the environmental variables. Contact a dev team member for these values
3. Run the application

    A) Run the Jar java -jar build/libs/stocktrade-1.0.jar

    Or

    B) Run the application from the main file with intellij
3. The application will now be running on port 8080 with context path /stock-trade

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

---

## Swagger

All the requests for the system can be seen using the swagger ui
available [here](http://localhost:8080/stock-trade/swagger-ui.html)
Paste the auth token from the login into the authorize button (No prefix just the token) and then all requests will be
authorized
