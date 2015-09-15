Remote matching module [VERSION 1.0]
=======================

* Implements [v1.0 of the MME specification](https://github.com/ga4gh/mme-apis/tree/v1.0a).

#####Installation (some experience with PhenoTips/XWiki required):
 - Install Java 1.7 or higher.
 - Build and install [PhenomeCentral.org](https://github.com/phenotips/phenomecentral.org/).
 - Build this project with `mvn install`
 - Manually run `mvn install` in the `standalone-patch` directory to get all the jars in one place; copy all the separate jars from the `standalone-patch/patch` into the PhenomeCentral webapp's `WEB-INF/lib` directory.
 - [Import](http://platform.xwiki.org/xwiki/bin/view/AdminGuide/ImportExport#HImportingXWikipages) the UI (```ui/target/*.xar```) through the PhenoTips administration interface.
 - Configure a remote server in the administration. To communicate with self, `http://localhost:8080/rest/remoteMatcher/`.

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
