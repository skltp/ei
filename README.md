# EI - SKLTP Engagemangs Index
Projektet är byggd med Spring-boot och Apache Camel.<br/>
Mer information om EI: https://skl-tp.atlassian.net/wiki/spaces/SKLTP/pages/8323234/EI+-+Engagemangsindex <br/>
 
 **Bygga projektet:<br/>**
 _mvn clean install_<br/>
 **Bygga projektet med testtäckning (rapporten hittas i report modulen):<br/>**
 _mvn clean verify -Ptest-coverage_
 
 **För att starta projektet lokalt för test:**
 1. Starta _EiTeststubApplication_ spring-boot applikation under _skltp-ei-teststub_ modulen.
 2. Starta _EiApplication_ spring-boot applikation under _skltp-ei-applikation_ modulen.
 3. Kör SOAP-UI tester som hittas under ./test/functional/SoapUI
 
Default startar EI med en H2 inmemory databas och en embedded ActiveMQ instans.<br/>
 
 
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
 
 [//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)
 
 
   [Konfigurering]: <doc/config/config.md>

