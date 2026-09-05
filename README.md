# woodstox-cve-demo

Demonstrates the dependency-chain impact of fixing **CVE-2022-40152** (a
denial-of-service vulnerability in [woodstox-core](https://github.com/FasterXML/woodstox),
fixed in `6.4.0` and `5.4.0`).

The application depends on an old version of `jackson-dataformat-xml` — the
"parent library" — which itself declares a dependency on `woodstox-core:6.0.3`.
Upgrading `woodstox-core` to `6.4.0` to patch the CVE without also updating the
parent library is a dependency-convergence violation, and the build fails until
`jackson-dataformat-xml` is updated to a version that declares a compatible
woodstox version.

## Version map used by this demo

| `jackson-dataformat-xml` | Declared `woodstox-core` | CVE-2022-40152 |
|--------------------------|--------------------------|----------------|
| 2.10.2                   | 6.0.3                    | VULNERABLE     |
| 2.13.5                   | 6.4.0                    | fixed          |

## Prerequisites

- JDK 11+ (demo verified on JDK 21)
- Maven 3.6+ (demo verified on 3.9.9)

## Project layout

```
pom.xml
src/main/java/com/example/woodstoxdemo/App.java
```

`App` parses a payment XML document with Jackson's `XmlMapper` (which uses
Woodstox as its StAX engine) and prints the resolved library versions plus the
CVE status.

## The relevant parts of `pom.xml`

The parent library and the explicit woodstox version are controlled by two
properties:

```xml
<properties>
    <!-- The parent library: an OLD Jackson XML module that pulls woodstox 6.0.3 -->
    <jackson.version>2.10.2</jackson.version>
    <!-- Explicit pin of the transitively-required woodstox-core CVE target -->
    <woodstox.version>6.0.3</woodstox.version>
</properties>
```

The build-failure behaviour is enforced by `maven-enforcer-plugin`'s
`dependencyConvergence` rule, which fails whenever different versions of the
same artifact appear in the dependency tree:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <executions>
        <execution>
            <id>enforce-dependency-convergence</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <dependencyConvergence>
                        <excludes>
                            <!-- jackson 2.10.x pins stax2-api 4.2.1 while woodstox
                                 6.0.x declares 4.2; that skew is not part of this demo. -->
                            <exclude>org.codehaus.woodstox:stax2-api</exclude>
                        </excludes>
                    </dependencyConvergence>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The project is currently left in the fixed state (Phase 3 below).

## Phase 1 — successful build with woodstox 6.0.3

`jackson.version=2.10.2`, `woodstox.version=6.0.3` (this is the exact combo
declared by the parent library, so convergence holds).

```bash
mvn clean verify
mvn dependency:tree -Dincludes=com.fasterxml.woodstox:woodstox-core
mvn compile exec:java
```

Expected:

```text
BUILD SUCCESS

\- com.fasterxml.woodstox:woodstox-core:jar:6.0.3:compile

Parsed         : Payment{txnId='PX-1001', amount=129.99 USD, status='APPROVED'}
Jackson (parent): 2.10.2
Woodstox (engine): 6.0.3
CVE-2022-40152 : VULNERABLE (woodstox < 5.4.0 or 6.0.0-6.3.x)
```

## Phase 2 — failed build: woodstox 6.4.0, parent not updated

The only `pom.xml` change:

```xml
<woodstox.version>6.0.3</woodstox.version>
```
becomes
```xml
<woodstox.version>6.4.0</woodstox.version>
```

`jackson.version` stays `2.10.2`.

> Note: Maven's "nearest wins" mediation silently resolves woodstox to 6.4.0,
> overriding the parent's 6.0.3, and `javac` accepts it (try
> `mvn compile -Denforcer.skip=true`). The convergence rule is what turns this
> hidden version skew into a build failure.

```bash
mvn clean verify
```

Expected:

```text
[ERROR] Dependency convergence error for com.fasterxml.woodstox:woodstox-core:jar:6.0.3 paths to dependency are:
[ERROR] +-com.example:woodstox-cve-demo:jar:1.0.0
[ERROR]   +-com.fasterxml.jackson.dataformat:jackson-dataformat-xml:jar:2.10.2:compile
[ERROR]     +-com.fasterxml.woodstox:woodstox-core:jar:6.0.3:compile
[ERROR] and
[ERROR] +-com.example:woodstox-cve-demo:jar:1.0.0
[ERROR]   +-com.fasterxml.woodstox:woodstox-core:jar:6.4.0:compile
[ERROR]
BUILD FAILURE
```

The failure tells you the app now resolves `woodstox-core 6.4.0` while the
parent library `jackson-dataformat-xml 2.10.2` still declares `6.0.3` — so the
parent library must be updated too.

## Phase 3 — successful build: woodstox 6.4.0 after updating the parent

Two `pom.xml` changes:

```xml
<jackson.version>2.10.2</jackson.version>
<woodstox.version>6.0.3</woodstox.version>
```
becomes
```xml
<jackson.version>2.13.5</jackson.version>
<woodstox.version>6.4.0</woodstox.version>
```

`jackson-dataformat-xml:2.13.5` declares `woodstox-core:6.4.0`, so the tree
converges again.

```bash
mvn clean verify
mvn dependency:tree -Dincludes=com.fasterxml.woodstox:woodstox-core,com.fasterxml.jackson.dataformat:jackson-dataformat-xml
mvn compile exec:java
```

Expected:

```text
BUILD SUCCESS (DependencyConvergence passed)

+- com.fasterxml.jackson.dataformat:jackson-dataformat-xml:jar:2.13.5:compile
\- com.fasterxml.woodstox:woodstox-core:jar:6.4.0:compile

Parsed         : Payment{txnId='PX-1001', amount=129.99 USD, status='APPROVED'}
Jackson (parent): 2.13.5
Woodstox (engine): 6.4.0
CVE-2022-40152 : NOT vulnerable
```

## Key takeaway

Maven performs no compile-time compatibility check on dependency versions, so
pinning woodstox 6.4.0 alone will not break `javac`. In a real project, rely on
a `dependencyConvergence`-style enforcer rule in CI so that a version skew like
"new woodstox + old parent library" fails the build with a clear, actionable
message instead of shipping silently.



## Security Scanner Setup
##

https://trivy.dev/docs/latest/guide/advanced/self-hosting/#vex-hub

