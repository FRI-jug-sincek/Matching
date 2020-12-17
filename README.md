# Apartments
Zahteve za namestitev:<br/>
  * Ukaz za dodajanje baze:<br/>
	docker run -d --name pg-apartments -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=apartments -p 5432:5432 postgres:13


## Prerequisites

```bash
mvn clean package
//poženi docker image users
java -jar users-api-1.0.0-SNAPSHOT.jar

//docker building
docker build -f Dockerfile_with_maven_build -t frijugsincek/users:latest .
docker push frijugsincek/users:latest   

docker network create cimr

docker run -d --name users -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -p 5432:5432 --network cimr postgres:13
```

geslo za povezavo na elephantdb bazo je treba ročno nastavit v k8s configuraciji dokler ne uspostavimo secretov
