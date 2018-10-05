Build:

./gradlew clean build


To build executable jar:


./gradlew :dms:shadowJar


To run:

java -jar dms/build/libs/dms-0.0.1-SNAPSHOT-fat.jar

To run integration tests (located in dms/src/itest/java):

./gradlew clean itest --info