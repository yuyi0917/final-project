## Upgrade to Java 21 (manual steps)

This repository doesn't show a Maven/Gradle build file, so the recommended path is a manual JDK install + using javac/java to compile and run projects.

Steps (Windows / PowerShell):

1) Install JDK 21
- Download a JDK 21 distribution (Eclipse Temurin / Adoptium, Azul, Microsoft Build of OpenJDK, etc.).
  - Adoptium (Temurin) downloads: https://adoptium.net/
  - Microsoft Build of OpenJDK: https://learn.microsoft.com/en-us/java/openjdk/download
- Install using the MSI and note the installation path (e.g. `C:\Program Files\Java\jdk-21`).

2) Set JAVA_HOME for current session and persist it for new sessions
- Use the included helper script `tools\set-java21.ps1` (run PowerShell as Administrator if you need to persist env vars for other users):

```powershell
# Example (run in PowerShell):
.\tools\set-java21.ps1 -JdkPath 'C:\Program Files\Java\jdk-21'

# Verify
java -version
javac -version
```

Note: `setx` is used only to persist `JAVA_HOME`. Persistent modification of PATH via `setx` is risky (can truncate PATH). If you want to add the JDK bin to the persistent PATH, add it manually in System Properties > Environment Variables or run a careful setx command.

3) Compile all student projects using JDK 21
- Use the included script `tools\compile-all.ps1` which will find `Main.java` files and compile each directory using the active `javac`:

```powershell
# Run in repo root
.\tools\compile-all.ps1
```

4) Run a compiled Main
- After a successful compile, change into the folder containing `Main.class` and run:

```powershell
java -cp . Main
```

5) For Maven or Gradle projects (if you add them later)
- Maven (pom.xml): set property `maven.compiler.source` and `maven.compiler.target` or `java.version` to `21` and ensure `maven-compiler-plugin` uses 21.
- Gradle: set `java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }` or `sourceCompatibility = '21'` for older scripts.

6) If you want me to attempt an automated upgrade using the upgrade tool: I tried but the automated upgrade tool is blocked by your Copilot plan. If you prefer that route, you can either upgrade the plan or allow me to proceed with the manual edits described above.

If you'd like, I can:
- Create/modify Maven/Gradle build files to target Java 21 for one or more subprojects.
- Try the automated upgrade again if you have access to the required Copilot plan.
- Walk through any compilation errors and fix code incompatibilities caused by the Java upgrade.

Next step suggestions: install JDK 21, run `tools\set-java21.ps1`, then `tools\compile-all.ps1`. Share any errors and I'll iterate fixes.