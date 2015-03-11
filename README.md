# advertiser-api-client
A Java Client for Advertiser API

#Before using
To use the code for obtaining the data for your program you need to edit the authentication information and groupBy parameter
* CONNECT_ID
* SECRET_KEY
* GROUP_BY  
Ask your Zanox contact person for your credentials.

#Build
mvn install

#Run
java -jar target/advertiser-api-client-1.0-SNAPSHOT.jar PROGRAM_ID CONNECT_ID SECRET_KEY GROUP_BY

e.g.
java -jar target/advertiser-api-client-1.0-SNAPSHOT.jar 1803 576F47B42F2819102E7A b9A63Af80b9D47+8ae84a3929776ba/9479fCE46 day