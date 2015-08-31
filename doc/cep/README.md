# Cepheus-CEP

- [Introduction](#introduction)
- [API Overview](#api-overview)
- [JSON Configuration](configuration.md)
- [Mapping Context Entities to Events](mapping.md)

## Introduction

Cepheus-CEP provides a Complex Event Processor (CEP) at the gateway level with a NGSI 9/10 interface for the FIWARE Data Handling GE.

Cepheus-CEP allows to locally process basic events (from data provided by sensors) and generate higher-level aggregated events.
All input and output of events is done though HTTP requests conforming to the NGSI information model.

## API Overview

Cepheus-cep has two APIs:

- the admin REST endpoint provides access to the current configuration and means to update it.
- the NGSI endpoints provides the means for communication with other NGSI components (Context Providers and Context Brokers).

### Admin endpoint

The admin endpoint defines a single REST endpoint where the whole configuration is available. The endpoint path is `/v1/admin/config`.
The endpoint accepts two HTTP verbs : `GET` and `POST`.

**GET v1/admin/config**

This endpoint returns the actual configuration as a JSON object with a `200 Ok` status code.

It can also return a `404 Not found` code, if no configuration is available.

Example:

    curl -H 'Accept: application/json' http://localhost:8080/v1/admin/config

**POST v1/admin/config**

This endpoint applies a new configuration given in the body as JSON.

The endpoint will return `200 Ok` on a successful operation, or `400 Bad Request` if the new configuration cannot be applied.

Once the new configuration has been successfully applied to the CEP, the configuration is persisted on disk.
If the Cepheus-cep is later restarted, it will automatically load the last configuration on startup.

Example:

    cat config.json | curl -H 'Accept: application/json' -H 'Content-Type: application/json' -d @-

See the [JSON Configuration](configuration.md) section about the content of the JSON configuration

### NGSI endpoints

Cepheus-CEP only supports the subset of NGSI standard operations it needs to communicate with other NGSI components.

It can receive updates to Context Elements, mapped as incoming events, by two (non exclusive) methods :

- by receiving directly updates though the `ngsi10/updateContext` (or `v1/updateContext`) operation.
- as a subscriber by receiving `ngsi10/notifyContext` (or `v1/notifyContext`) after it registered to a context broker using `ngsi10/subscribeContext`.

Cepheus-CEP will then publish updates to Context Entities (on outgoing events) using the `ngsi10/updateContext` requests to a broker.
