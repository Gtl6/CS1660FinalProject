FROM openjdk:8
COPY . /usr/src/myapp
COPY INPUT_FILES/ /usr/src/myapp
WORKDIR /usr/src/myapp
RUN apt-get update \
	&& apt-get -y install libxext6 \
	&& apt-get -y install libxtst6 \
	&& apt-get -y install libxrender1 
CMD ["java", "-jar", "Java-App-1.0-SNAPSHOT-jar-with-dependencies.jar"]