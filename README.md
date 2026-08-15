# Box Delivery Service

A REST API for managing delivery boxes and loading items into them.

## Tech Stack

- Java 17
- Spring Boot 4.0.7
- Spring Data JPA
- H2
- Lombok
- Maven

## Design Approach

Standard layered architecture:

```text
Controller → Service → Repository → Database
```

- **Controller** — handles HTTP requests, request validation, and responses.
- **Service** — enforces business rules: battery/weight checks, state transitions, `txref` uniqueness.
- **Repository** — Spring Data JPA persistence for boxes and items.

The API is exposed under `/api/v1` so future breaking changes don't affect existing clients.

## Running the Application

Clone the repository and run:
git clone `git clone https://github.com/<your-username>/box-delivery-service.git`

cd box-delivery-service

```bash
./mvnw spring-boot:run
```

The API will be available at:

```
http://localhost:8080/api/v1
```

## Database

The application uses a file-based H2 database for development. No initial data is required by the application, so no seed data is preloaded. Boxes and items are created through the REST API.

**H2 Console:** `http://localhost:8080/h2-console`

**Connection details:**

| Field | Value                       |
|---|-----------------------------|
| JDBC URL | `jdbc:h2:file:./data/boxdb` |
| Username | `boxdelivery`               |
| Password | *leave it empty*            |

## API Endpoints

All endpoints below are relative to:

`http://localhost:8080/api/v1`

### Create a Box

`POST /boxes`

**Request:**

```json
{
  "txref": "BOX-001",
  "weightLimit": 500,
  "batteryLevel": 80
}
```

**Response:**

```json
{
  "id": "generated-uuid",
  "txref": "BOX-001",
  "weightLimit": 500,
  "batteryLevel": 80,
  "state": "IDLE"
}
```

### Load Items into a Box

`POST /boxes/{boxId}/items`

**Request:**

```json
{
  "items": [
    {
      "name": "Medicine-01",
      "weight": 200,
      "code": "MED_001"
    },
    {
      "name": "Food_02",
      "weight": 100,
      "code": "FOOD_002"
    }
  ]
}
```

### Get Box Items

`GET /boxes/{boxId}/items`

### Get Available Boxes

`GET /boxes/available-for-loading`

Returns boxes that:

- Are in `IDLE` state
- Have at least 25% battery

### Get Box Battery Level

`GET /boxes/{boxId}/battery`

## Business Rules

- `txref` must be unique and cannot exceed 20 characters.
- Box weight limit must be between 1 and 500 grams.
- Battery level must be between 0 and 100%.
- A box must have at least 25% battery to be loaded.
- A box must be in `IDLE` state before loading.
- At least one item must be provided when loading.
- Total submitted item weight cannot exceed the box's weight limit.
- Item names may contain letters, numbers, hyphens and underscores.
- Item codes may contain uppercase letters, numbers and underscores.
- Loading transitions the box from `IDLE` → `LOADING` → `LOADED`.
- Loading is transactional.

## Error Handling

Invalid requests and business-rule violations return appropriate HTTP errors with a JSON response.

**Example:**

```json
{
  "timestamp": "2026-08-13T18:44:04",
  "status": 400,
  "error": "Bad Request",
  "message": "Box battery level must be at least 25% to load",
  "path": "/api/v1/boxes/generated-uuid/items"
}
```

## Testing

Run the test suite:

```bash
./mvnw clean test
```

The tests cover box creation, loading, validation, retrieval, battery checks and error-handling scenarios. Service-layer tests use Mockito to isolate business logic from persistence dependencies, while the application context test uses an isolated test database configuration.

## Assumptions

Since the task description leaves some details open, the following assumptions were made:

- `txref` uniquely identifies a box.
- A newly created box starts in the `IDLE` state.
- Battery level is an integer percentage (0–100).
- Weight is represented in grams; a box's weight limit may be configured up to the 500g maximum.
- Items are loaded through a single request; the box transitions `IDLE` → `LOADING` while items are persisted, then to `LOADED` once all items are saved successfully.
- Loading is transactional; if the loading operation fails, the box/item changes are rolled back.
- Box and item IDs are generated UUIDs.
