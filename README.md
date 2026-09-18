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

## Full Demo Instructions

Full demo instructions are in the DEMOSCRIPT.md file.

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

## Key takeaway

Maven performs no compile-time compatibility check on dependency versions, so
pinning woodstox 6.4.0 alone will not break `javac`. In a real project, rely on
a `dependencyConvergence`-style enforcer rule in CI so that a version skew like
"new woodstox + old parent library" fails the build with a clear, actionable
message instead of shipping silently.



## Security Scanner Setup
##

https://trivy.dev/docs/latest/guide/advanced/self-hosting/#vex-hub

