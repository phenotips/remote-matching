Remote matching module [VERSION 0.1]
=======================

*A working beta version of Remote Matching project. It is highly probable that the code contains a large number of bugs.
It is very close to the specification, mainly lacking the support for ```periodic``` requests and the functionality needed for such requests.
The code is largely **undocumented and uncommented**; it is likely that fixing this and bugs will be the next stage of the project, before implementing ```preiodic``` requests*

#####Implemented features:
 - Functionality almost completely to specification when doing synchronous requests.
 - Can answer requests with ```email``` as ```responseType``` (sends JSON dump).
 - Can send requests with either ```email```, ```inline```, or ```asynchronous``` set as ```responseType```.
 - Receive asynchronous results (see known issues section)

#####Non-implemented features:
 - Respond with asynchronous results
 - Properly react to responses, other than when sending synchronous requests.

#####Installation (some experience with PhenoTips/XWiki required):
 - Install Java 1.7 or higher.
 - Build PhenomeCentral.org
 - Copy all the separate jars from the ```standalone-patch/patch```
 - Import the UI (```ui/```)
 - Copy the single javascript file in the ```war``` to appropriate directory
 - Configure a remote server in the administration. To communicate with self, ```http://localhost:8080/rest/remoteMatcher/```.
 Any/no token will work locally. Check mutually accepted.

#####Known issues:
 - There's a security hole; the REST server executes XWiki commands as Admin.
 - The disorders are not shown in the UI, because there is a problem with retrieving disorder id.
 (The issue seems to be outside of the project's codebase)
 [The match is ```null```, but it probably shouldn't be](https://github.com/phenotips/patient-network/blob/master/similarity-data-impl/src/main/java/org/phenotips/data/similarity/internal/RestrictedDisorderSimilarityView.java#L66)
 - The asynchronous request results are stored properly **iff** the id sent in the originating request is returned in the response.

######Unrelated known issues:
 - commons-lang3 has 2 versions in the WEB-INF/lib. This causes an error in PatientSheet. Delete the older.

<br>
= ```core/```<br>
||=```api/```              Interfaces only.<br>
||=```server/```<br>
||=```client/```           For interactions with HTTP client<br>
||=```hibernate/```        Hibernate entities. Important to keep them all together.<br>
= ```adapters/```         Contains static converters (classes containing only static methods) and wrappers around those classes for convenience<br>
= ```standalone-patch```<br>
||=```patch```             This is where all the jars are copied to for convenience.<br>
= ```war/```              Javascript files.<br>
= ```ui/```               XWiki .xml files.<br>
