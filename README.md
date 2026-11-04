# Campaign Management

A REST API that lets a seller create, edit and delete an advertising campaign for each product they sell. Campaign funds are deducted from the seller's Emerald account.

## Tech stack

- Java 21
- Spring Boot 4 (Web MVC, Data JPA, Validation)
- H2 in-memory database
- Lombok
- JUnit 5, Mockito, AssertJ, MockMvc

## Running the application

You need JDK 21 with `JAVA_HOME` pointing to it. Maven is provided by the wrapper.

```bash
cd backend
./mvnw spring-boot:run
```

On Windows, use `.\mvnw spring-boot:run`.

The API is available at `http://localhost:8085/api`.

The database is created on startup and filled from `backend/src/main/resources/data.sql`: one seller with a balance of 1000.00, 4 products, 8 towns and 12 keywords. All changes are lost when the application stops.

The H2 console is available at `http://localhost:8085/h2-console`, with the JDBC URL `jdbc:h2:mem:campaigns`.

## Running the tests

```bash
cd backend
./mvnw test
```

- **Unit tests** cover the service layer with mocked repositories, including charging and refunding funds.
- **Integration tests** send HTTP requests through MockMvc against the full application context and the H2 database.

## API

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/seller` | Current seller and Emerald balance | 200 |
| GET | `/api/products` | Products | 200 |
| GET | `/api/towns` | Towns | 200 |
| GET | `/api/keywords?query=` | Up to 10 keywords matching the query, for typeahead | 200 |
| GET | `/api/campaigns` | Campaigns | 200 |
| POST | `/api/campaigns` | Create a campaign | 201 |
| PUT | `/api/campaigns/{id}` | Update a campaign | 200 |
| DELETE | `/api/campaigns/{id}` | Delete a campaign | 204 |

### Creating a campaign

```bash
curl -X POST http://localhost:8085/api/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop campaign",
    "productId": 1,
    "keywordIds": [1, 3],
    "bidAmount": 1.00,
    "fund": 100.00,
    "status": "ON",
    "townId": 1,
    "radiusKm": 10
  }'
```

| Field | Required | Rules |
|---|---|---|
| `name` | Yes | Not blank |
| `productId` | Yes, on create only | Existing product without a campaign |
| `keywordIds` | Yes | At least one existing keyword |
| `bidAmount` | Yes | At least 0.10, up to 2 decimal places |
| `fund` | Yes | Greater than 0, up to 2 decimal places, covered by the balance |
| `status` | Yes | `ON` or `OFF` |
| `townId` | No | Existing town |
| `radiusKm` | Yes | Greater than 0 |

`PUT` takes the same fields except `productId`, because a campaign stays assigned to its product.

### Errors

Errors share one format. `fieldErrors` is filled in only when validation fails.

```json
{
  "message": "Validation failed",
  "fieldErrors": {
    "bidAmount": "must be greater than or equal to 0.1"
  }
}
```

The text of validation messages follows the default locale.

| Status | When |
|---|---|
| 400 | Invalid fields, unknown keyword, unreadable request body |
| 404 | Product, town or campaign not found |
| 409 | Insufficient funds, or the product already has a campaign |

## Decisions

The brief leaves these points open, so I chose the following:

- **Seller:** there is one seeded seller and no login. The first seller in the database is treated as the current one.
- **One campaign per product:** a second campaign for the same product returns 409. Deleting a campaign frees the product.
- **Minimum bid:** 0.10, since the brief requires a minimum without giving a value.
- **Keywords:** can only be chosen from the seeded list.
- **Town:** optional, because the brief does not mark it as mandatory.
- **Funds on create:** the whole fund is deducted from the balance.
- **Funds on edit:** only the difference is charged or refunded.
- **Funds on delete:** the whole fund is refunded, because this project does not track money already spent by a campaign.
- **Insufficient funds:** the request is rejected with 409 and nothing changes.
