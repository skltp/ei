# EI - SKLTP Engagemangs Index
Projektet är byggd med Spring-boot och Apache Camel.<br/>
Mer information om EI: https://inera.atlassian.net/wiki/spaces/SKLTP/pages/3187858114/EI+-+Engagemangsindex <br/>
 
 **Bygga projektet:<br/>**
 _mvn clean install_<br/>
 **Bygga projektet med testtäckning (rapporten hittas i report modulen):<br/>**
 _mvn clean verify -Ptest-coverage_
 
 **För att starta projektet lokalt för test:**
 1. Starta _EiTeststubApplication_ spring-boot applikation under _skltp-ei-teststub_ modulen.
 2. Starta _EiApplication_ spring-boot applikation under _skltp-ei-applikation_ modulen.
 3. Kör SOAP-UI tester som hittas under ./test/functional/SoapUI

```
Punkt 2:
Menyalternativ för uppstart i IntelliJ IDEA:
* skltp-ei-teststub    > Plugins > spring-boot > spring-boot:run 
  // Startar en console-attached instans av teststub-appen.
* skltp-ei-application > Plugins > spring-boot > spring-boot:run
  // Startar en console-attached instans av application-appen i en ny run-flik.

Nedstängning:
* skltp-ei-application : Stopp-knappen i verktygsfältet.
* skltp-ei-teststub > Plugins > spring-boot > spring-boot:stop
```
 
Default startar EI med en H2 in-memory databas och en embedded ActiveMQ instans.<br/>
 
 
 ## Moduler
* **skltp-ei-application**<br/>
Spring-boot applikation som startar både ei-frontend och ei-backend
* **skltp-ei-backend**<br/>
Spring-boot applikation med EIs backend funktionalitet.
* **skltp-ei-frontend**<br/>
Spring-boot applikation med EIs frontend funktionalitet
* **skltp-ei-schemas**<br/>
Innehåller SOAP service scheman som nyttjas av EI
* **skltp-ei-common**<br/>
Gemensamma utiltys som nyttjas av både EI front- och backendapplikationer
* **skltp-ei-data-model**<br/>
Innehåller JPA datamodel för EI samt JPA Data repositories.
* **skltp-ei-teststub<br/>**
För enklare testning finns teststub för tjänsterna:
    * GetLogicalAdreessesByServiceContract (när EI hämtar "Subscribers" av ProcessNotifications)<br/>
    * ProcessNotification (när EI skickar ProcessNotifications till "subscribers")<br/>
* **test**<br/>
SOAP-UI tester
* **report**<br/>
Modul med syfte att sammanställa en Jacoco test rapport.

 ## Dokumentation referenser
 - [Konfigurering]
 - [Write Lock – Runbook för databasmigrering]
 
 ## Write Lock (skrivlås)
 
 EI backend har en write lock-funktion som tillfälligt stoppar all skrivning till databasen genom att suspendera Camel-rutterna `backend-process-route` och `backend-collection-route`. Meddelanden buffras i ActiveMQ under tiden låset är aktivt.
 
 Funktionen används vid migrering av databaskluster, där man vill undvika dubbelskrivning medan VIP:en flyttas mellan gamla och nya noden.
 
 ### Hanteringsendpoints (managementport 8083)
 
 | Endpoint | Beskrivning |
 |---|---|
 | `GET /skltp-ei/writelock/enable` | Aktiverar skrivlåset – suspenderar process- och collect-rutter |
 | `GET /skltp-ei/writelock/disable` | Inaktiverar skrivlåset – återupptar rutterna |
 | `GET /skltp-ei/writelock/status` | Returnerar JSON med låsstatus och ruttstatus |
 
 ### Åtkomst
 
 Managementporten (8083) exponeras **inte** utanför klustret. Använd något av följande:
 
 - **`kubectl exec`** (rekommenderat): `kubectl exec -it <pod> -- wget -qO- http://localhost:8083/skltp-ei/writelock/status`
 - **`kubectl port-forward`**: `kubectl port-forward <pod> 8083:8083` och sedan `curl http://localhost:8083/skltp-ei/writelock/status` lokalt
 
 ### Egenskaper
 
 - Idempotent: upprepade anrop till enable/disable är ofarliga
 - FindContent (läsning) påverkas **inte** av skrivlåset
 - Frontend kan fortsätta ta emot uppdateringar under tiden (JMS-buffring)
 - Konfigurerbara route-ID:n via `ei.route.id.process` och `ei.route.id.collect`
 
 Se [Write Lock – Runbook för databasmigrering] för steg-för-steg-instruktioner.

 [//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)
 
 
   [Konfigurering]: <doc/config/config.md>
   [Write Lock – Runbook för databasmigrering]: <doc/db-disable-doc/db-migration-writelock-runbook.md>

