# Konfiguration av EI

EI är en spring-boot applikation och kan konfigureras enligt de sätt spring-boot föreskriver

### Application.properties ###
Spring-boot property fil som ligger under resources i jaren. Inställningarna kan överlagras enligt de sätt som Spring-boot föreskriver. 
Utöver dessa properties finns möjlighet att konfigurera de komponenter som används och stödjer spring boot. exempelvis Camel, JPA, ActiveMQ m.m.

|Nyckel|Defaultvärde/Exempel|Beskrivning|
|----|------------------|---------|
| server.port | 8881 | Spring-boot server port |
| update.webservice.url | http://localhost:8081/skltp-ei/update-service/v1 |URL för Update webbtjänsten |
| processnotification.webservice.url | http://localhost:8081/skltp-ei/notification-service/v1 | URL för ProcessNotification webbtjänsten |
| findcontent.webservice.url | http://localhost:8082/skltp-ei/find-content-service/v1 | URL för FindContent webtjänsten |
| ei.hsa.id | ei-hsa-id | EIs egna HSA id |
| ei.alternative.hsa.id | ei-hsa-id | Alternativ till EIs HSA-ID |
| vp.hsa.id | vp-hsa-id | VP's sender-ID för interna anrop |
| vp.instance.id | vp-instance-id | VP's instans-ID för interna anrop |
| getlogicaladdresses.serviceEndpointUrl | http://localhost:8080/vp/getlogicaladdreesses | Utgående URL till GLABSC tjänsten |
| getlogicaladdresses.logicalAddress | $\{vp.hsa.id\} | HSA id till GLABSC producenten |
| getlogicaladdresses.vpSenderId | $\{vp.hsa.id\} | VP's sender-ID för interna anrop |
| getlogicaladdresses.vpInstanceId | $\{vp.instance.id} | VP's instans-ID för interna anrop |
| getlogicaladdresses.connectTimeoutMs | 5000 | Connect timeout ms för GLABSC anrop |
| getlogicaladdresses.requestTimeoutMs | 5000 | Request timeout ms för GLABSC anrop |
| processnotification.serviceEndpointUrl | http://localhost:8080/vp/processnotification | Utgående URL för ProcessNotifications |
| processnotification.vpSenderId | $\{vp.hsa.id\} | VP's sender-ID för interna anrop |
| processnotification.vpInstanceId | $\{vp.instance.id\} | VP's instans-ID för interna anrop |
| subscriber.cache.file.name |  | Filnamn för lokal cache av subscribers |
| subscriber.cache.timeToLiveSeconds | 7200 | Timeout innan subscriber cachen förnyas genom anrop med GLABSC tjänsten  |
| update-notification.not.allowed.hsaid.list | \${ei.hsa.id\},$\{vp.hsa.id\} | Otillåtna HSA-IDn för att undvika recirculation av anrop  |
| collect.queue.name | skltp.ei.collect | Namn på collect kön i AMQ |
| collect.queue.completion.size | 1000 | Antal anrop i kön innan den collectas och skickas till process kön |
| collect.queue.completion.timeout | 5 | Max sekunder meddelande kan ligga i collect kön innan de skickas till processkön  |
| update.collect.threshold | 1 | Threshold för antal engagemang i ett anrop för att de ska hamna direkt i Process kön |
| process.queue.name | skltp.ei.process | Namn på processkön i AMQ  |
| activemq.broker.url | vm://localhost?broker.persistent=false | URL till AMQ broker  |

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   MySQL example
   spring.datasource.url=jdbc:mysql://localhost:33099/dogs
   spring.datasource.password=<ENTER_PASSWORD_HERE >
   spring.datasource.username=root
   spring.datasource.driver-class-name=com.mysql.jdbc.Driver
   sspring.datasource.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
   spring.datasource.jpa.hibernate.ddl-auto=update