# Demo Script for Lightwell Code Remediation with JFrog Artifactory Demonstration

## Synopsis

The demo is intended to show the problem that Lightwell is solving for. The demo shows a Java application, "Parasol Payment Gateway" using Maven as the build tool.

This is a very simple, very small Java application using a single dependency (Jackson). We show that this introduces a set of child dependencies, and that a CVE detected in one of those child dependencies (woodstox) is not trivial to correct. We then show how Lightwell makes the remediation simpler with JFrog Artifactory.

## Demo Preparation

To run this demo, you need to:

1. Install OpenJDK11 (includes Maven)
2. Clone the demo repo locally
3. Install trivy (https://trivy.dev/docs/latest/getting-started/installation/#install-script-official)
4. Preinstall the "security-bom" locally by running:

``` bash
cd packaging-pom
mvn install
```
5. If you're rerunning the demo, clear the maven cache with:

```bash
mvn dependency:purge-local-repository
```
6. Set up JFrog Artifactory as per the instructions in README.md
7. In Artifactory, remove the lightwell-validated and lightwell-remediated repos from the lightwell-enabled-maven virtual repo.
8. Configure Maven to include the lightwell repositories:
  1. Edit the file ~/.m2/settings.xml
  2. Add the following repo content, replacing the artifactory username, password token and hostname as configured:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd" xmlns="http://maven.apache.org/SETTINGS/1.2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <mirrors>
    <mirror>
      <id>maven-default-http-blocker</id>
      <url>http://[[[JFROG HOSTNAME]]]:8082</url>
      <mirrorOf>dummy</mirrorOf>
      <blocked>false</blocked>
    </mirror>
  </mirrors>
  <servers>
    <server>
      <username>[[[JFROG USERNAME]]]</username>
      <password>[[[JFROG TOKEN]]]</password>
      <id>central</id>
    </server>
  </servers>
  <profiles>
    <profile>
      <repositories>
        <repository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>central</id>
          <name>lightwell-enabled-maven</name>
          <url>http://[[[JFROG HOSTNAME]]]:8082/artifactory/lightwell-enabled-maven</url>
        </repository>
      </repositories>
      <id>artifactory</id>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>artifactory</activeProfile>
  </activeProfiles>
</settings>
```

## Demo Process and Suggested Description

### The Initial Build

1. Show the App.java file briefly, indicating that it simply parses some XML payment information and logs it to the output.
2. Show the default pom.xml file, indicating the dependency on the jackson-xml library.
3. Open a Terminal.
4. Show the dependency tree, which includes a bunch of child dependencies, by running:

```mvn dependency:tree```

5. Run the application:

```mvn compile exec:java```

6. Confirm that the application has parsed some XML and written the values to stdout.

### The CVE Issue

1. Run the security scanner:

```trivy -q fs .```

2. Call out one CVE in particular: CVE-2022-40152. This is specific to the child woodstox library.
3. Open a browser to the CVE page (https://nvd.nist.gov/vuln/detail/cve-2022-40152)
4. Show the Affected Products lists woodstox
5. Open the woodstox library page on Maven (https://mvnrepository.com/artifact/com.fasterxml.woodstox/woodstox-core)
6. Show that the CVE is corrected in 6.4.0
7. Update the pom.xml to the pinned woodstox version:

```cp pom_1_pinned_woodstox.xml pom.xml```

8. Open pom.xml and show the new pinned version 6.4.0 of woodstox.
9. Clean the maven repo and verify that the new config is OK:

```mvn clean verify```

10. Show that the error is caused because version 6.4.0 of woodstox conflicts with the version of jackson-xml.

### The Upgrade Problem

1. Explain what happens if we need to upgrade the parent library.
2. Show the page that indicates that v2.13.5 of jackson-xml includes 6.4.0 or woodstox (https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-xml/2.13.5/dependencies)
3. Update the pom:

```cp pom_2_pinned_upgrade_jackson.xml pom.xml```

4. Show the new pom and the dependencies. Point out that we've had to update the parent library to correct a CVE in the child dependency. And what happens as more and more CVEs are discovered?
5. Verify that the system builds now:

```mvn clean verify```
```mvn dependency:tree```
```mvn compile exec:java```

### The Lightwell Solution

1. Now show an easier way. What if we could remediate the CVE without changing any dependent libraries?
2. Show the Lighwell Demo remediation page (https://console.redhat.com/lightwell/demo). Show the Java Remediated repo, and the woodstox-core 6.0.3 remediation
3. Update the pom:

```cp pom_3_lightwell.xml pom.xml```

4. Show the Maven configuration (~/.m2/settings.xml) showing that we're using an existing Maven repository hosted in JFrog Artifactory.
5. Now open Artifactory and show that the current lightwell-enabled-maven repo only includes the maven-central repo.
6. Add the lightwell-remediated and lightwell-validated repos to the lightwell-enabled-maven repo and discuss the "layering" of repos.
7. Show the new pom, and that we've got our original jackson library, with a local import of a new "security packaging BOM".
8. Show packaging-pom/pom.xml and show that this can be managed by our DevSecOps team, and imported by all app teams.
9. Verify that this works:

```mvn clean verify```
```mvn dependency:tree```
```mvn compile exec:java```

10. Show the [OSV file](https://osv.dev/vulnerability/RHLW-2026-403d1e66c95ef9bb) for this fix. Note that trivy doesn't yet consume this OSV feed, but that we'll soon also be publishing VEX files.
11. Show the lightwell-woodstox-core.openvex file (a sample) which indicates that the CVE in question was corrected by the Lightwell patched library.

12. Rerun our security scanner.

```trivy -q --vex lightwell-woodstox-core.openvex fs .```

13. Show that CVE-2022-40152 is no longer listed, as we've now patched it.

```trivy -q --vex lightwell-woodstox-core.openvex fs . | grep 40152```

## Accompanying Resources

Slide deck to go along with the demo, including YouTube links to a recording of this demo, are here (Red Hat internal):

https://docs.google.com/presentation/d/1H5tMrGp9n_Prq_jT571ntMGYDndQL4_o3tSdecbC4V8/edit?slide=id.g3e71e2e86e1_0_1375&pli=1#slide=id.g3e71e2e86e1_0_1375

