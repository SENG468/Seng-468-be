# Seng-468-be

---
## Docker

To build and run the docker container

1. `./gradlew bootJar` This builds the jar that will be put into the docker
2. `docker build -t stocktrade .` This creates the container image. Must be run from the `Seng-468-be/` directory
3. `docker start -p {hostPort}:{8080}` The app can then be access at the host port
4. ^C (Ctrl. C) to end the program
---