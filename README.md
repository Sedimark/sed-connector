# SEDIMARK Connector

Connector for integrating data providers and consumers with the SEDIMARK marketplace. Extends EDC Connector functionality and bridges external systems with the SEDIMARK decentralized components.

## API Tutorial

Welcome to the tutorial for the Connector API. This guide walks you through the complete data exchange lifecycle: from asset publication to data transfer, using the Connector Management API. The tutorial will show you which requests to use, but the full body and HTTP options asociated with those requests can be found in the Postman collection.

### API Specification

Before you begin, you can familiarize yourself with the [Connector Management API](https://eclipse-edc.github.io/Connector/openapi/management-api/#/).

---

### Lifecycle Overview

The process is divided into four phases:

1. **Publication** – Publishing assets, policies, and contract definitions.
2. **Discovery** – Querying the catalog to discover contracts.
3. **Negotiation** -  Negotiating contracts to reach agreements.
4. **Data Transfer** – Initiating and completing data transfers.

All requests are assumed to be preceded by the endpoint of your connector. For example, if the request says:
```http
POST /management/v3/assets
```

It is short for:
```http
POST https://<your-connector-endpoint>/management/v3/assets
```

---

### 1. Publication Phase
NOTE: In SEDIMARK, this phase will be managed by the Offering Manager. Do not use these requests directly. They are presented in this tutorial only for documentation purposes.

#### 1.1 Publish an Asset (Provider)

```http
POST /management/v3/assets
```

The asset includes a JSON object named "dataAddress":
```json
"dataAddress": {
    "type": "HttpData",
    "name": "Test asset",
    "baseUrl": "<private-endpoint>",
    "proxyPath": "true"
}
```
The private endpoint where the connector will find the data can be set in the "baseUrl" parameter. This will NOT be published and will never be available to consumers. A consumer that negotiates a contract and starts a data transfer will instead obtain a public URL exposed by the provider's connector that will act as a proxy for the consumer to get the data.

#### 1.2 Define a Policy (Provider)

```http
POST /management/v3/policydefinitions
```

#### 1.3 Create a Contract Definition (Provider)

```http
POST /management/v3/contractdefinitions
```

---

### 2. Discovery Phase

#### 2.1 Request the Catalog (Consumer/Provider)

```http
POST /management/v3/catalog/request
```

This request can be performed by both provider and consumer. In the body of the request, a parameter called "counterPartyAddress" indicates whose catalog the connector is requesting. For example, to request the catalog of "provider1", this parameter would have the following value:
```json
"counterPartyAddress": "https://<provider1-connector-endpoint>/protocol",
```
Response contains available contract offers, such as:

```json
{
    "@id": "<UUID>",
    "@type": "dcat:Catalog",
    "dcat:dataset": [{
        "@id": "assetId",
        "@type": "dcat:Dataset",
        "odrl:hasPolicy": {
            "@id": "<ID required to start the negotiation>",
            "@type": "odrl:Offer",
            "odrl:permission": [],
            "odrl:prohibition": [],
            "odrl:obligation": []
        },
        "dcat:distribution": [
            {
                "@type": "dcat:Distribution",
                "dct:format": {
                    "@id": "HttpData-PULL"
                },
                "dcat:accessService": {
                    "@id": "<UUID>",
                    "@type": "dcat:DataService",
                    "dcat:endpointDescription": "dspace:connector",
                    "dcat:endpointUrl": "http://connector-provider-endpoint/protocol",
                    "dcat:endpointURL": "http://connector-provider-endpoint/protocol"
                }
            },
            {
                "@type": "dcat:Distribution",
                "dct:format": {
                    "@id": "HttpData-PUSH"
                },
                "dcat:accessService": {
                    "@id": "<UUID>",
                    "@type": "dcat:DataService",
                    "dcat:endpointDescription": "dspace:connector",
                    "dcat:endpointUrl": "http://connector-provider-endpoint/protocol",
                    "dcat:endpointURL": "http://connector-provider-endpoint/protocol"
                }
            }
        ],
        "name": "product description",
        "id": "assetId",
        "contenttype": "application/json"
    }],
    ...
}
```

#### 2.2 Request only a specific asset (Consumer/Provider)
If you know the asset ID in advance and just want to obtain your contract ID without having to request the entire catalog, you may use the following request:

```http
POST /management/v3/catalog/dataset/request
```

The body of the request includes a parameter that allows you to retrieve only the contracts associated with the desired asset:

```json
"@id": "assetId"
```

---

### 3. Negotiation phase
This phase is initiated by the consumer. In the catalog listing request, you may find the ID that your connector needs to start negotiating a contract. In the example above, it can be found in catalog -> "dcat:dataset" -> "odrl:hasPolicy" -> "@id". This information is also included in the Postman collection.

#### 3.1 Initiate a Contract Negotiation (Consumer)

```http
POST /management/v3/contractnegotiations
```

The connector will perform the negotiation internally and reach an agreement. Agreements reached can be listed by consumers and providers.

---

### Agreement Listing
Both parties can list the agreements they have reached at any time.

#### 3.1 List Contract Agreements (Consumer/Provider)

```http
POST /management/v3/contractagreements/request
```

Response contains contract agreements, such as:

```json
[
{
    "@type": "ContractAgreement",
    "@id": "<ID required to start the transfer>",
    "assetId": "assetId",
    "policy": {
        "@id": "policyId",
        "@type": "odrl:Agreement",
        "odrl:permission": [],
        "odrl:prohibition": [],
        "odrl:obligation": [],
        "odrl:assignee": "consumer",
        "odrl:assigner": "provider",
        "odrl:target": {
            "@id": "assetId"
        }
    },
    "contractSigningDate": 1746002718,
    "consumerId": "consumer",
    "providerId": "provider",
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
]
```

Note the ID of the agreement, because it will be necessary to start the data transfer process.

---

### Data Transfer Phase
Once a contract agreement has been reached, the ID of said agreement can be used to trigger a data transfer. This process needs to be initiated by the consumer before actually retrieving the data.

#### 4.1 Initiate a Transfer Process (Consumer)

```http
POST /management/v3/transferprocesses
```

This request will yield a transfer process ID that can be used as part of the URL in the following request.

```json
{
    "@type": "IdResponse",
    "@id": "<ID required to get the transfer parameters>",
    "createdAt": 1746520807086,
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
```

#### 4.2 Obtain the EDR (Consumer)

```http
GET /management/v3/edrs/transfer_process_id/dataaddress
```

The response will contain the necessary information for the consumer to finally retrieve the data:

```json
{
    "@type": "DataAddress",
    "type": "https://w3id.org/idsa/v4.1/HTTP",
    "endpoint": "http://<provider-endpoint>/public",
    "authType": "bearer",
    "endpointType": "https://w3id.org/idsa/v4.1/HTTP",
    "authorization": "eyJ...",
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
```

Note the "endpoint" and the "authorization" parameters.

#### 4.3 Retrieve the Data (Consumer)
Once the transfer completes, you may use the returned endpoint and authorization token in the EDR to obtain the asset.

```http
GET http://<provider-endpoint>/public
Authorization: eyJ...
```

## Examples of use

### 1. [Provider] List all of my available contracts
As a Provider, I have published many offerings (in SEDIMARK terms) and now I want to list all of the contracts I am offering to the marketplace. The request to use in this case is:

```http
POST /management/v3/catalog/request
```

In this case, I will be requesting my own Catalog. The body of the request will be:

```json
{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "counterPartyAddress": "http://<provider-endpoint>/protocol",
  "protocol": "dataspace-protocol-http"
}
```

NOTE: this is the exact same request that a Consumer would use to request my Catalog.

In order to get the details of all the offerings, it is recommended to use the Offering Manager instead.

### 2. [Consumer/Provider] List all of my agreements
As any Participant, I have reached many contract agreements and I want to list them. If I'm a Provider, I want to see the details of all of my sales. If I'm a Consumer, I want to see the details of all of my purchases. Request to use:

```http
POST /management/v3/contractagreements/request
```
```json
{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@type": "QuerySpec",
  "offset": 0,
  "limit": 10,
  "sortOrder": "DESC",
  "sortField": "contractSigningDate",
  "filterExpression": []
}
```

Several filtering and sorting parameters can be used, as well as pagination and limiting the number of concurrent results. The response will be a list of contract agreements, including who the Participants are, the ID of the associated asset, and the contract signing timestamp. In order to obtain details of the assets themselves, this request can be used to obtain a list of asset IDs, and then ask the Offering Manager for those IDs.

### 3. [Consumer] Buy and retrieve an asset
NOTE: this use case is also valid for Federated Learning assets.

As a Consumer, I have seen an asset that I'm interested in, and I want to go through the entire process of negotiation and transfer. I will have to use several requests that are interconnected. I will start by negotiating the contract with the ID that I got when I requested the Catalog:

```http
POST /management/v3/contractnegotiations
```
```json
{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@type": "ContractRequest",
  "counterPartyAddress": "http://<provider-endpoint>/protocol",
  "protocol": "dataspace-protocol-http",
  "policy": {
    "@context": "http://www.w3.org/ns/odrl.jsonld",
    "@id": "<contract_offer_id>", // contract-offer-id found in dcat:dataset.odrl:hasPolicy.@id
    "@type": "Offer",
    "assigner": "<provider>",
    "target": "<asset-id>"
  }
}
```
Response:
```json
{
    "@type": "IdResponse",
    "@id": "c003809f-6f9f-4e3a-b718-2dbd8184a903", // negotiation ID
    "createdAt": 1746539200208,
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
```

This initiates a background negotiation process. At some point the contract agreement will be ready. I will use the "simpler" option and simply poll my connector to know when the negotiation is finished. An easy way to do it is to list my contract agreements until I see the new one. In this example, I will filter the agreements by assetId so I will get either an empty list or the desired agreement:

```http
POST /management/v3/contractagreements/request
```
```json
{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@type": "QuerySpec",
  "offset": 0,
  "limit": 10,
  "sortOrder": "DESC",
  "sortField": "contractSigningDate",
  "filterExpression": [{
    "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" },
    "@type": "Criterion",
    "operandLeft": "assetId",
    "operator": "=",
    "operandRight": "<asset-id>" }] // insert here the asset ID
}
```
Response:
```json
[
    {
        "@type": "ContractAgreement",
        "@id": "e86c7f42-be1a-41e6-8f65-119afcb7443d", // contract agreement ID
        "assetId": "<asset-id>",
        "policy": {
            "@id": "39951bfc-66ae-4301-9286-599faeceb884",
            "@type": "odrl:Agreement",
            "odrl:permission": [],
            "odrl:prohibition": [],
            "odrl:obligation": [],
            "odrl:assignee": "consumer",
            "odrl:assigner": "provider",
            "odrl:target": {
                "@id": "<asset-id>"
            }
        },
        "contractSigningDate": 1746000778,
        "consumerId": "consumer",
        "providerId": "provider",
        "@context": {
            "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
            "edc": "https://w3id.org/edc/v0.0.1/ns/",
            "odrl": "http://www.w3.org/ns/odrl/2/"
        }
    }
]
```

There are other ways to obtain the contract agreement, like polling the negotiation ID that was in the response to our first request. This is only an alternative to the previous request:
```http
GET management/v3/contractnegotiations/<contract-negotiation-id>
```
Response:
```json
{
    "@type": "ContractNegotiation",
    "@id": "c003809f-6f9f-4e3a-b718-2dbd8184a903",
    "type": "CONSUMER",
    "protocol": "dataspace-protocol-http",
    "state": "FINALIZED",
    "counterPartyId": "provider",
    "counterPartyAddress": "http://<provider-endpoint>/protocol",
    "callbackAddresses": [],
    "createdAt": 1746539200208,
    "contractAgreementId": "e86c7f42-be1a-41e6-8f65-119afcb7443d", // contract agreement ID
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
```

In any case, I have obtained the contract agreement ID. I can now use this to initiate the transfer process:

```http
POST /management/v3/transferprocesses
```
```json
{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@type": "TransferRequestDto",
  "connectorId": "provider",
  "counterPartyAddress": "http://<provider-endpoint>/protocol",
  "contractId": "<contract-agreement-id>",
  "protocol": "dataspace-protocol-http",
  "transferType": "HttpData-PULL"
}
```
Response:
```json
{
    "@type": "IdResponse",
    "@id": "d05abe37-83dd-4612-9278-52e5c16ef1c6", // transfer process ID
    "createdAt": 1746520807086,
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
```
This starts a background transfer process. Similarly to the contract negotiation, at some point the EDR (which is what I need to get the data) will be ready. I will once again use polling to list all the transfer processes, filtering by asset ID:
```http
POST /management/v3/transferprocesses/request
```
```json
{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@type": "QuerySpec",
  "offset": 0,
  "limit": 10,
  "sortOrder": "DESC",
  "sortField": "stateTimestamp",
  "filterExpression": [{
    "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" },
    "@type": "Criterion",
    "operandLeft": "assetId",
    "operator": "=",
    "operandRight": "<asset-id>" }] // insert here the asset ID
}
```
Response:
```json
[
    {
        "@id": "d05abe37-83dd-4612-9278-52e5c16ef1c6", // transfer process ID
        "@type": "TransferProcess",
        "state": "STARTED", // state has to be STARTED
        "stateTimestamp": 1746520808021,
        "type": "CONSUMER",
        "callbackAddresses": [],
        "correlationId": "6c269cd0-709d-4d8c-bbf2-6ac3c11ebd1c",
        "assetId": "assetId",
        "contractId": "f1a79d49-1509-4eaa-9414-8fe321889040",
        "transferType": "HttpData-PULL",
        "@context": {
            "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
            "edc": "https://w3id.org/edc/v0.0.1/ns/",
            "odrl": "http://www.w3.org/ns/odrl/2/"
        }
    }
]
```
Once our polling yields a STARTED state, we can obtain the EDR:
```http
GET /management/v3/edrs/<transfer-process-id>/dataaddress
```
Response:
```json
{
    "@type": "DataAddress",
    "type": "https://w3id.org/idsa/v4.1/HTTP",
    "endpoint": "http://<provider-endpoint>/public", // endpoint
    "authType": "bearer",
    "endpointType": "https://w3id.org/idsa/v4.1/HTTP",
    "authorization": "eyJra...", // token
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "edc": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    }
}
```
NOTE: this request will yield an error if it is performed before the transfer process is in the STARTED state.

The endpoint and the token will be necessary to finally obtain the data. In the case of Federated Learning, the endpoint and token can be used to configure the client node, as this endpoint acts as a proxy towards the actual internal endpoint, which will be the server node.
```http
GET http://<provider-endpoint>/public
Authorization: eyJ...
```

## Deployment 

### Kubernetes 

To deploy a Sedimark connector instance in a Kubernetes cluster, follow these steps:

1. Source your environment with the necessary variables:

| Name                            | Description                                   | Example (in clear text)                           | base64 encoding needed|
|:--------------------------------|:----------------------------------------------|:--------------------------------------------------|:----------------------|
| CONNECTOR_NAMESPACE             | Kubernetes namespace to deploy in             | connector                                         | ❌                    |
| CONNECTOR_APP_NAME              | Application name used for Kubernetes resources| connector                                         | ❌                    |
| STORAGECLASS                    | Kubernetes storageclass for persistent volumes| nfs-storageclass                                  | ❌                    |
| CONNECTOR_PARTICIPANT_ID        | ID of connector                               | university-cantabria                              | ❌                    |
| CONNECTOR_KEYSTORE              | The content of your keystore file, in base64  | $(cat <path/to/keystore> \| base64)               | ✅                    |
| CONNECTOR_KEYSTORE_PASSWORD     | Keystore password                             | password123                                       | ✅                    |
| PUBLIC_NODE_DOMAIN              | Domain to expose the connector                | sedimark.example.eu                               | ❌                    |
| DOCKER_REGISTRY_CREDENTIALS     | Docker registry credentials                   | {"auths":{"registry.example.com":{"username":"user","password":"pass"}}} | ✅ |
| CONNECTOR_DOCKER_REGISTRY       | Path of the Docker image to use               | docker.io/edcconnector/connector-sedimark         | ❌                    |
| CONNECTOR_IMAGETAG              | Tag of the connector image to pull            | latest                                            | ❌                    |

2. Apply the manifests:

```bash 
cat ./deployment/kubernetes/*.yaml | envsubst | kubectl apply -f -
```
