# Konfiguration av EI Camel

EI är en Spring Boot-applikation som kan konfigureras enligt de sätt Spring Boot föreskriver.

### Application.properties ###
Spring Boot-propertyfil som ligger under resources i jar-filen. Inställningarna kan överlagras enligt de sätt som Spring Boot föreskriver. 
Utöver dessa properties finns möjlighet att konfigurera de komponenter som används och stödjer Spring Boot, exempelvis Camel, JPA, ActiveMQ m.m.

|Nyckel|Defaultvärde/Exempel|Beskrivning|
|----|------------------|---------|
| server.port | 8881 | Spring-boot server port |
| update.webservice.url | http://localhost:8081/skltp-ei/update-service/v1 |URL för Update webbtjänsten |
| processnotification.webservice.url | http://localhost:8081/skltp-ei/notification-service/v1 | URL för ProcessNotification webbtjänsten |
| findcontent.webservice.url | http://localhost:8082/skltp-ei/find-content-service/v1 | URL för FindContent webtjänsten |
| management.endpoints.web.exposure.include | hawtio,jolokia,health | Behöver inkluder 'hawtio,jolokia' för att hawtio ska köras. 'health' behövs för probes |
| management.endpoint.health.probes.enabled | true | Exponera liveness/readiness probes |
| management.endpoint.health.show-details | always | Visa detaljer om health indicators |
| management.health.livenessState.enabled | true | Aktivera inbyggd livess-indikator |
| management.health.readinessState.enabled | true | Aktivera inbyggd readiness-indikator |
| spring.jmx.enabled | true | Slå på JMX för Spring, så att det går att konfigurera t.ex. köer i Hawtio |
| hawtio.authentication.enabled | true | Sätt till 'false' för att stänga av autentisering i Hawtio |
| hawtio.external.loginfile | src/test/resources/users.properties | Sökväg till fil med Hawtio-användare och lösenord. Användare behöver tillhöra rollen 'user' för att kunna logga in. Formatet beskrivs på: https://wiki.eclipse.org/Jetty/Tutorial/Realms#HashLoginService |
| ei.hsa.id | ei-hsa-id | EIs egna HSA id |
| ei.alternative.hsa.id | ei-hsa-id | Alternativ till EIs HSA-ID |
| vp.hsa.id | vp-hsa-id | VP's sender-ID för interna anrop |
| vp.instance.id | vp-instance-id | VP's instans-ID för interna anrop |
| getlogicaladdresses.serviceEndpointUrl | http://localhost:8080/vp/getlogicaladdreesses | Utgående URL till GLABSC tjänsten |
| getlogicaladdresses.logicalAddress | $\{vp.hsa.id\} | HSA id till GLABSC producenten |
| getlogicaladdresses.vpSenderId | $\{vp.hsa.id\} | VP's sender-ID för interna anrop |
| getlogicaladdresses.vpInstanceId | $\{vp.instance.id\} | VP's instans-ID för interna anrop |
| getlogicaladdresses.connectTimeoutMs | 5000 | Connect timeout ms för GLABSC anrop |
| getlogicaladdresses.requestTimeoutMs | 5000 | Request timeout ms för GLABSC anrop |
| processnotification.serviceEndpointUrl | http://localhost:8080/vp/processnotification | Utgående URL för ProcessNotifications |
| processnotification.vpSenderId | $\{vp.hsa.id\} | VP's sender-ID för interna anrop |
| processnotification.vpInstanceId | $\{vp.instance.id\} | VP's instans-ID för interna anrop |
| subscriber.cache.file.name |  | Filnamn för lokal cache av subscribers |
| subscriber.cache.timeToLiveSeconds | 7200 | Timeout innan subscriber cachen förnyas genom anrop med GLABSC tjänsten  |
| subscriber.cache.reset.url | http://localhost:8083/skltp-ei/resetcache | Adress för att tömma prenumerantcachen | 
| subscriber.cache.status.url | http://localhost:8083/skltp-ei/subscriber/status | Adress för att få status på prenumeranter |
| ei.status.url | http://localhost:8083/skltp-ei/status | Adress för att få status på EI |
| update-notification.not.allowed.hsaid.list | \${ei.hsa.id\},$\{vp.hsa.id\} | Otillåtna HSA-IDn för att undvika recirkulation av anrop  |
| collect.queue.name | skltp.ei.collect | Namn på collect kön i AMQ |
| collect.queue.completion.size | 1000 | Antal anrop i kön innan den collectas och skickas till process kön |
| collect.queue.completion.timeout | 5 | Max sekunder meddelande kan ligga i collect kön innan de skickas till processkön  |
| update.collect.threshold | 1 | Threshold för antal engagemang i ett anrop för att de ska hamna direkt i Process kön |
| process.queue.name | skltp.ei.process | Namn på processkön i AMQ  |
| activemq.broker.url | vm://localhost?broker.persistent=false | URL till AMQ broker  |
| activemq.broker.user | admin | Användarnamn för ActiveMQ-användare |
| activemq.broker.password | secret | Lösenord för ActiveMQ-användare |
| activemq.broker.maximum-redeliveries | 2 | Antal leveransförsök som görs innan meddelanden hamnar i dead letter-kön. Standardinställningen är 0. |
| activemq.broker.redelivery-delay | 5000 | Tid mellan leveransförsök i millisekunder. Standardinställningen är 1000 ms. |
| activemq.broker.use-exponential-backoff | false | Sätt till true om exponential backoff ska användas, annars false. |
| activemq.broker.backoff-multiplier | 3 | Om exponential backoff ovan är satt till true, kommer varje omsändningsförsök n att fördröjas med föregående delay \* multiplier. |
| activemq.broker.maximum-redelivery-delay | 600000 | Maximal tid mellan leveransförsök i millisekunder, om exponential backoff används. Standardinställningen är 600000 ms (10 minuter). |
| activemq.broker.notification.maximum-redeliveries | ${activemq.broker.maximum-redeliveries} | Antal leveransförsök specifikt för notification-köer (prenumeranter). Standard är att använda samma värde som för interna köer. |
| activemq.broker.notification.redelivery-delay | ${activemq.broker.redelivery-delay} | Tid mellan leveransförsök specifikt för notification-köer (prenumeranter). Standard är att använda samma värde som för interna köer. |
| activemq.broker.notification.use-exponential-backoff | ${activemq.broker.use-exponential-backoff} | Sätt till true om exponential backoff ska användas specifikt för notification-köer (prenumeranter). Standard är att använda samma som för interna köer. |
| activemq.broker.notification.backoff-multiplier | ${activemq.broker.backoff-multiplier} | Specifikt värde för notification-köer (prenumeranter). Standard är att använda samma värde som för interna köer. |
| activemq.broker.notification.maximum-redelivery-delay | ${activemq.broker.maximum-redelivery-delay} | Maximal tid mellan leveransförsök i millisekunder, om exponential backoff används, för notification-köer (prenumeranter). Standardinställningen är att använda samma som för interna köer. |
| camel.component.activemq.broker-url | ${activemq.broker.url} | Adress till ActiveMQ | 
| camel.component.activemq.trust-all-packages | true | Standard för ActiveMQ är att inte tillåta serialisering av godtyckliga Javaobjekt. Denna inställning ändrar detta beteende. | 

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   MySQL example
   spring.datasource.url=jdbc:mysql://localhost:33099/dogs
   spring.datasource.password=<ENTER_PASSWORD_HERE >
   spring.datasource.username=root
   spring.datasource.driver-class-name=com.mysql.jdbc.Driver
   sspring.datasource.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
   spring.datasource.jpa.hibernate.ddl-auto=update