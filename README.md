# Omnixys Observability

Enterprise-grade observability foundation for Spring Boot microservices.

This package provides a standardized, production-ready observability layer across the Omnixys ecosystem, including distributed tracing, metrics, and seamless OpenTelemetry integration.

---

## ✨ Features

- 🔍 **Distributed Tracing (OpenTelemetry)**
- 📡 **OTLP Export (Tempo, Jaeger, etc.)**
- 📊 **Micrometer Metrics + Prometheus**
- 🧠 **Automatic Trace Context Propagation**
- ⚙️ **Spring Boot Auto-Configuration**
- 🧩 **Omnixys Unified Property Model**
- 🚀 **Zero-Boilerplate Integration**

---

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>com.omnixys.observability</groupId>
    <artifactId>omnixys-observability</artifactId>
    <version>0.2.0</version>
</dependency>
````

---

## ⚙️ Configuration

Minimal configuration:

```yaml
omnixys:
  observability:
    service-name: address-service
    otlp:
      endpoint: http://tempo:4318
```

---

## 🧠 How it works

This package automatically configures:

* OpenTelemetry SDK
* OTLP exporter
* Trace context propagation (W3C)
* Micrometer metrics
* Prometheus endpoint (via Spring Boot Actuator)

All components are auto-wired via Spring Boot auto-configuration.

---

## 🔗 Trace Context Propagation

The library ensures:

* automatic extraction of incoming trace context
* automatic propagation across service boundaries
* compatibility with Kafka, HTTP, and async flows

---

## 📊 Metrics

Metrics are exposed via:

```
/actuator/prometheus
```

Compatible with:

* Prometheus
* Grafana
* Omnixys Observability Stack

---

## 🏗️ Architecture

This package is designed as:

* reusable library
* zero-runtime configuration overhead
* consistent observability layer across all microservices

---

## 🚀 Production Usage

Designed for:

* high-scale microservices
* distributed event-driven systems (Kafka)
* full trace visibility across services

---

## 🧪 Requirements

* Java 25
* Spring Boot 4.x

---

## 🏢 Organization

Omnixys Technologies
[https://omnixys.com](https://omnixys.com)

---

## 📄 License

GNU General Public License v3.0 or later
[https://www.gnu.org/licenses/gpl-3.0-standalone.html](https://www.gnu.org/licenses/gpl-3.0-standalone.html)
