Remote Matching service
=======================

**Soon, this repository will be a beautiful little butterfly, with spotless, glossy, polished wings.
But right its a cocoon oozing with imperfection**

How to install/use:
 - Build PhenomeCentral.org with PatientNetwork
 - Copy all the separate jars
 - Import the UI (```ui/```)
 - Copy the single javascript file in the ```war``` to appropriate directory
 (To be able to use the javascript [which uses jquery] a timeout setting of require.js needs to be changed.
 This setting does not belong in this project, and should be made in the PhenoTips repo. Until the changes are made
 there, please do this manually. If clicking the lookup button causes the page to jump, and no search happens, this
 is the problem.)
 - Configure a remote server in the administration. To communicate with self, ```http://localhost:8080/rest/remoteMatcher/```.
 Any/no token will work. Check mutually accepted.

Known issues/problematic code:
 - The HTTPClient needs to be upgraded. Include 4.3.3 instead/with 4.2.5. [httpcore, httpcore-nio]
 - There's a possible security hole; the REST server executes commands as Admin.
 - In the UI there's a weird bug, where sometimes a ```<p>``` is present between the list of remote servers and "Remote
 Databases" heading. Disappears on refresh.

Unrelated known issues:
 - common-lib has 2 versions in the WEB-INF/lib. This causes an error in PatientSheet. Delete the older.

Path                     | Details
-------------------------|---------------------------------
+```ui/```	             | XWiki .xml files.
+```war/```	             | Javascript files.
+```rest/```	         | Java REST server files and client files for sending requests.
-+```api/```             | Besides interfaces, contains the only script service
-+```server/```          |
-+```client/```          |
+```adapters/```         | These will format the data and do other processing tasks
+```standalone-patch```  | Does not currently work
