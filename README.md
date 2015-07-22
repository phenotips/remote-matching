Remote matching module [VERSION 1.0]
=======================

* Imnplements v1.0 of the MME specification.

#####Installation (some experience with PhenoTips/XWiki required):
 - Install Java 1.7 or higher.
 - Build PhenomeCentral.org.
 - Manunally "mvn install" in the ```standalone-patch``` directory to get all the jars in one place; copy all the separate jars from the ```standalone-patch/patch```.
 - Import the UI (```ui/```).
 - Configure a remote server in the administration. To communicate with self, ```http://localhost:8080/rest/remoteMatcher/```.

#####Folder structure
= ```core/```<br>
||=```api/```              Interfaces only.<br>
||=```common/```<br>
||=```server/```           Server part: handles incoming requests<br>
||=```client/```           Client part: sends outgoign requests and provides a service for result siplay in UI.<br>
||=```hibernate/```        Request storage subsystem.<br>
= ```standalone-patch```<br>
||=```patch```             This is where all the jars are copied to by Maven.<br>
= ```war/```<br>
= ```ui/```                XWiki .xml files.<br>
