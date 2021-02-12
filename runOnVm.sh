sudo docker pull eetar1/stocktrade
sudo docker run --network="host" -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e JWT_SECRET=super-secret eetar1/stocktrade
