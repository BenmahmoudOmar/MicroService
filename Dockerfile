FROM openjdk:17-jdk-slim

# Install Python 3
RUN apt-get update && apt-get install -y python3 python3-pip

# Install necessary Python dependencies
RUN pip3 install pandas scikit-learn

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file into the container
COPY target/user-service.jar /app/user-service.jar

# Copy the application.properties file into the container
COPY src/main/resources/application.properties /app/application.properties

# Copy the Python script into the container
COPY predict_anomaly.py /app/predict_anomaly.py

# Expose the port your application runs on
EXPOSE 8089

# Specify the command to run your application
CMD ["java", "-jar", "user-service.jar"]
