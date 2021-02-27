docker pull eetar1/stocktrade
docker run --network="host" -p 8080:8080 -e MONGO_HOST=127.0.0.1 -e SPRING_PROFILES_ACTIVE=prod -e JWT_SECRET=super-secret eetar1/stocktrade
