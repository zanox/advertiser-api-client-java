# advertiser-api-client
A Java Client for Advertiser API

#Before using
To use the code for obtaining the data for your program you need to obtain the authentication information first:
* CONNECT_ID
* SECRET_KEY
Ask your Zanox contact person for your credentials.

#Build
mvn install

#Run
java -jar target/advertiser-api-client-1.0-SNAPSHOT.jar  --[header|url] PROGRAM_ID CONNECT_ID SECRET_KEY GROUP_BY

e.g.
java -jar target/advertiser-api-client-1.0-SNAPSHOT.jar --header 1803 476F47B42F2819102E7A a9A63Af80b9D47+8ae84a3929776ba/9479fCE46 day
Note that this example will not go through authentication, because secrete key and connect id are faked.