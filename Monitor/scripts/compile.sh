mvn clean install assembly:single
mv target/*dependencies.jar bin/
rm target/*.jar
