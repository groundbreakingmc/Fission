# Fission - Fast File Reader Library

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-brightgreen.svg)](https://openjdk.org/)
[![Wiki](https://img.shields.io/badge/docs-Wiki-informational.svg)](https://github.com/groundbreakingmc/Fission/wiki)

A high-performance, zero-dependency library for fast file reading and character stream processing.

## Quick Start

```java
// Read entire file
String content = Fission.readString("file.txt");

// Stream processing
try (CharSource source = Fission.chars("file.txt")) {
    while (source.hasNext()) {
        char ch = (char) source.read();
    }
}
```

## Performance Highlights

- **20x faster** than BufferedReader for character reading
- **87x faster** for parsing operations
- **O(1) complexity** for parser operations

Tested on Java 17

## Documentation

For complete API documentation, usage examples, and advanced topics, see the **[Fission Wiki](https://github.com/groundbreakingmc/Fission/wiki)**.

## Key Features

- No checked exceptions
- Thread-unsafe for maximum performance
- Advanced parsing capabilities
- Dual in-memory and file-based implementations

## Installation

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.groundbreakingmc</groupId>
    <artifactId>Fission</artifactId>
    <version>1.0.0</version>
</dependency>
```
```kotlin
repositories {
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}
dependencies {
    implementation("com.github.groundbreakingmc:Fission:1.0.0")
}
```

## Contributing

Contributions welcome! Please read
our [Contributing Guide](https://github.com/groundbreakingmc/Fission/wiki/Contributing).

---

**Fission** - Split files faster than atoms! ⚛️