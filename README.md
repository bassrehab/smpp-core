# SMPP Core

Modern Java 21 implementation of the SMPP (Short Message Peer-to-Peer) protocol.

## Features

- **Java 21**: Leverages virtual threads, records, sealed interfaces
- **Type-safe PDUs**: Immutable records with builders
- **Complete protocol support**: SMPP 3.3, 3.4, and 5.0
- **High performance**: Netty 4.1.x based transport
- **Observable**: Micrometer metrics integration

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
    <artifactId>smpp-server</artifactId>
    <version>1.0.0</version>
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
- [Documentation](https://smppgateway.io/docs)
- [SMPP Specification](https://smpp.org/)
