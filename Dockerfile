FROM openjdk:17
COPY "/target/PiProject-0.0.1-SNAPSHOT.jar" "piproject-dk.jar"
EXPOSE 8011
ENTRYPOINT ["java", "-jar", "piproject-dk.jar"]
