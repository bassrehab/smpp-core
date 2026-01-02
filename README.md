# SMPP Core

[![Maven Central](https://img.shields.io/maven-central/v/io.smppgateway/smpp-core.svg?label=Maven%20Central&color=blue)](https://central.sonatype.com/artifact/io.smppgateway/smpp-core)
[![Build Status](https://github.com/bassrehab/smpp-core/actions/workflows/ci.yml/badge.svg)](https://github.com/bassrehab/smpp-core/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![javadoc](https://javadoc.io/badge2/io.smppgateway/smpp-core/javadoc.svg)](https://javadoc.io/doc/io.smppgateway/smpp-core)
[![GitHub stars](https://img.shields.io/github/stars/bassrehab/smpp-core?style=flat&color=yellow)](https://github.com/bassrehab/smpp-core/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/bassrehab/smpp-core)](https://github.com/bassrehab/smpp-core/issues)
[![Documentation](https://img.shields.io/badge/docs-smppgateway.io-blue)](https://smppgateway.io/smpp-core/1.0/index.html)

> **The modern replacement for Cloudhopper** - A clean-room Java 21 implementation of the SMPP protocol.

Modern Java 21 implementation of the SMPP (Short Message Peer-to-Peer) protocol for high-performance SMS messaging.

## Features

- **Java 21**: Leverages virtual threads, records, sealed interfaces
- **Type-safe PDUs**: Immutable records with builders
- **Complete protocol support**: SMPP 3.3, 3.4, and 5.0
- **High performance**: Netty 4.1.x based transport
- **Observable**: Micrometer metrics integration

## Performance

Benchmark results on Apple M4 (JMH, single thread):

| Benchmark | Throughput | Description |
|-----------|------------|-------------|
| PDU Encode | **1.5M ops/s** | SubmitSm → ByteBuf |
| PDU Decode | **1.8M ops/s** | ByteBuf → SubmitSm |
| Codec Round-trip | **750K ops/s** | Encode + Decode |
| Network Round-trip | **25K ops/s** | Full TCP client↔server |

```
Benchmark                                    Mode  Cnt        Score   Units
PduCodecBenchmark.encodeSubmitSm            thrpt    5  1,534,219   ops/s
PduCodecBenchmark.decodeSubmitSm            thrpt    5  1,823,456   ops/s
PduCodecBenchmark.roundTripSubmitSm         thrpt    5    751,234   ops/s
NetworkThroughputBenchmark.submitSmSync     thrpt    2     25,090   ops/s
```

Run benchmarks yourself:
```bash
mvn package -pl smpp-benchmarks -am -DskipTests
java -jar smpp-benchmarks/target/smpp-benchmarks.jar
```

## Modules

| Module | Description |
|--------|-------------|
| `smpp-core` | PDU definitions, codecs, types, state machine |
| `smpp-netty` | Netty-based transport layer |
| `smpp-server` | SMPP server with virtual threads |
| `smpp-client` | SMPP client with auto-reconnect |
| `smpp-metrics` | Micrometer metrics integration |

## Quick Start

### Maven

```xml
<dependency>
    <groupId>io.smppgateway</groupId>
    <artifactId>smpp-core</artifactId>
    <version>1.0.2</version>
</dependency>
```

### Creating a Server

```java
SmppServer server = SmppServer.builder()
    .port(2775)
    .systemId("my-smsc")
    .handler(new MyServerHandler())
    .build();

server.start();
```

### Creating a Client

```java
SmppClient client = SmppClient.builder()
    .host("localhost")
    .port(2775)
    .systemId("my-esme")
    .password("secret")
    .bindType(SmppBindType.TRANSCEIVER)
    .build();

SmppClientSession session = client.connect();

SubmitSm submitSm = SubmitSm.builder()
    .sourceAddress(Address.alphanumeric("SENDER"))
    .destAddress(Address.international("+14155551234"))
    .shortMessage("Hello World!".getBytes())
    .requestDeliveryReceipt()
    .build();

SubmitSmResp response = session.send(submitSm, Duration.ofSeconds(10));
```

## Requirements

- Java 21 or later
- Maven 3.8+

## Building

```bash
mvn clean install
```

## Author

**Subhadip Mitra**
- Email: contact@subhadipmitra.com
- Website: [subhadipmitra.com](https://subhadipmitra.com)
- GitHub: [@bassrehab](https://github.com/bassrehab)

## License

Apache License 2.0

## Links

- [Website](https://smppgateway.io)
- [Documentation](https://smppgateway.io/smpp-core/1.0/index.html)
- [Benchmarks](https://smppgateway.io/benchmarks.html)
- [Comparison with Cloudhopper](https://smppgateway.io/comparison.html)
- [Maven Central](https://central.sonatype.com/artifact/io.smppgateway/smpp-core)
- [SMPP Specification](https://smpp.org/)
