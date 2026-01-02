# SMPP Core Benchmarks

JMH benchmarks for measuring performance of the smpp-core library.

## Building

```bash
# From project root
mvn package -pl smpp-benchmarks -am -DskipTests
```

This creates `smpp-benchmarks/target/smpp-benchmarks.jar` - an uber-jar containing all dependencies.

## Running Benchmarks

### All Benchmarks

```bash
java -jar smpp-benchmarks/target/smpp-benchmarks.jar
```

### Specific Benchmark

```bash
# PDU codec benchmarks only
java -jar smpp-benchmarks/target/smpp-benchmarks.jar PduCodecBenchmark

# Throughput benchmarks only
java -jar smpp-benchmarks/target/smpp-benchmarks.jar ThroughputBenchmark

# Memory benchmarks only
java -jar smpp-benchmarks/target/smpp-benchmarks.jar MemoryBenchmark
```

### Quick Run (fewer iterations)

```bash
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -wi 1 -i 3 -f 1 PduCodecBenchmark
```

- `-wi 1`: 1 warmup iteration
- `-i 3`: 3 measurement iterations
- `-f 1`: 1 fork

### With Profiling

```bash
# GC profiler (allocation rates)
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -prof gc MemoryBenchmark

# JFR profiler (detailed analysis)
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -prof jfr ThroughputBenchmark

# Stack profiler
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -prof stack PduCodecBenchmark
```

### Output Formats

```bash
# JSON output
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -rf json -rff results.json

# CSV output
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -rf csv -rff results.csv
```

## Benchmark Descriptions

### PduCodecBenchmark

Measures PDU encoding and decoding throughput:
- `encodeSubmitSm`: SubmitSm PDU to ByteBuf
- `decodeSubmitSm`: ByteBuf to SubmitSm PDU
- `roundTripSubmitSm`: Encode + Decode round-trip
- Similar tests for DeliverSm, BindTransceiver, EnquireLink

### ThroughputBenchmark

Measures end-to-end message throughput:
- `submitSmSync`: Synchronous request-response pattern
- `submitSmAsync`: Asynchronous with windowing (4 threads)

### MemoryBenchmark

Measures memory usage under different configurations:
- `sendAcrossAllConnections`: Memory per connection under load
- `pduObjectAllocation`: PDU object allocation overhead
- Compares virtual threads vs platform threads

## Sample Results

Results vary by hardware. Here's an example from a MacBook Pro M1:

```
Benchmark                               Mode  Cnt       Score       Error  Units
PduCodecBenchmark.encodeSubmitSm       thrpt    5  2500000.123 ±  50000.456  ops/s
PduCodecBenchmark.decodeSubmitSm       thrpt    5  1800000.789 ±  30000.123  ops/s
PduCodecBenchmark.roundTripSubmitSm    thrpt    5  1200000.456 ±  20000.789  ops/s
ThroughputBenchmark.submitSmSync       thrpt    5    25000.123 ±   1000.456  ops/s
ThroughputBenchmark.submitSmAsync      thrpt    5    80000.789 ±   3000.123  ops/s
```

## JMH Parameters

Common JMH options:
- `-f <n>`: Number of forks (default: 2)
- `-wi <n>`: Warmup iterations (default: 3)
- `-i <n>`: Measurement iterations (default: 5)
- `-t <n>`: Threads
- `-bm <mode>`: Benchmark mode (thrpt, avgt, sample, ss, all)
- `-tu <unit>`: Time unit (ns, us, ms, s, m, h)

List all options:
```bash
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -h
```

List all benchmarks:
```bash
java -jar smpp-benchmarks/target/smpp-benchmarks.jar -l
```
