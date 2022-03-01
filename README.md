#Reproducer of a quarkus issue
https://github.com/quarkusio/quarkus/issues/24030

##Start with :
docker-compose.yml  
mvn clean install
#### On module core :
mvn quarkus:dev

#### To send data into the API :
Start the main of Benchmark  
It will send a batch of 3000 logs each 0.5 second between 50 threads.


The error append once I got ~10k entries in Log table.
This can depend on the computer configuration.

The rabbitMQ monitoring can be accessed at the URL :  
http://localhost:15672/#/queues

User : guest  
Password : guest

In Java 17  
Quarkus 2.7.0