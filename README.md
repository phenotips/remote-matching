Remote matching service
=======================

**The repository has only VERY basic functionality (Synchronous request/response).
However, the project structure is philosophically correct, and any future developments will have a nurturing environment.**

What it can do:
 - Functionality almost completely to specification when doing synchronous requests.
 - Can answer requests with ```email``` as ```responseType``` (sends JSON dump)
 - Can send requests with either ```email```, ```inline```, or ```asynchronous``` set as ```responseType```

What it cannot do:
 - Receive asynchronous results
 - Respond with asynchronous results
 - Properly react to responses, other than when sending synchronous requests.

How to install/use:
 - Install Java 1.7 or higher
 - Build PhenomeCentral.org
 - Copy all the separate jars from the ```standalone-patch/patch```
 - Import the UI (```ui/```)
 - Copy the single javascript file in the ```war``` to appropriate directory
 - Configure a remote server in the administration. To communicate with self, ```http://localhost:8080/rest/remoteMatcher/```.
 Any/no token will work locally. Check mutually accepted.

Known issues/problematic code:
 - There's a security hole; the REST server executes XWiki commands as Admin.
 - The disorders are not shown in the UI, because there is a problem with retrieving disorder id.

Unrelated known issues:
 - commons-lang3 has 2 versions in the WEB-INF/lib. This causes an error in PatientSheet. Delete the older.

= ```core/```<br>
||=```api/```              Interfaces only.<br>
||=```server/```<br>
||=```client/```           For interactions with HTTP client<br>
||=```hibernate/```        Hibernate entities. Important to keep them all together.<br>
= ```adapters/```         <p>Contains static converters (classes containing only static methods) and wrappers around those classes for convenience</p><br>
= ```standalone-patch```<br>
||=```patch```             This is where all the jars are copied to for convenience.<br>
= ```war/```              Javascript files.<br>
= ```ui/```               XWiki .xml files.<br>
