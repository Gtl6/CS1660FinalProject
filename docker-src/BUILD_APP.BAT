cd my-app
call mvn clean compile assembly:single
cd target
copy Java-App-1.0-SNAPSHOT-jar-with-dependencies.jar ..\..\Java-App-1.0-SNAPSHOT-jar-with-dependencies.jar /Y
cd ..
cd ..
echo DONE
