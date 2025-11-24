# FlightSpring - Reactive Flight Booking System

FlightSpring is a high-performance, reactive flight booking API built using **Spring Boot WebFlux** and **MongoDB**. It handles complex booking flows, inventory management, and itinerary generation using non-blocking, asynchronous streams.

---

## Tasks & Features Implemented

The project focuses on building a scalable backend system for an airline reservation platform. Key tasks performed include:

### 1. Admin Module
* **Inventory Management:** Created endpoints to onboard airlines and add flight inventory.
* **Validation:** Implemented strict validation for flight schedules (e.g., arrival must be after departure) and pricing.

### 2. Search Engine
* **Flight Search:** Implemented a search algorithm to find flights between airports for a specific date range.
* **Reactive Streams:** Utilized `Flux` to stream search results efficiently without blocking the main thread.

### 3. Booking Engine (Core Logic)
* **Atomic Booking:** Implemented transactional-like behavior for booking tickets.
* **Trip Types:** Support for both **One-Way** and **Round-Trip** bookings.
* **Concurrency Handling:** Included checks for seat availability and duplicate seat numbers to prevent overbooking.
* **Seat Management:** Automatically decrements available seats upon booking and increments them upon cancellation.
* **PNR Generation:** Generates unique PNRs (Passenger Name Records) to track itineraries.

### 4. User Management & History
* **User Creation:** Automatically creates users upon their first booking if they do not exist.
* **Booking History:** Fetches all past and upcoming trips associated with a user's email.
* **Ticket Retrieval:** Fetches full itinerary details using the PNR.

### 5. Cancellation System
* **Cancellation Rules:** Implemented logic to prevent cancellation if the flight is within 24 hours.
* **Refund Logic:** Restores inventory (seat count) when a booking is successfully cancelled.

### 6. Testing & Quality Assurance
* **Unit Testing:** Wrote comprehensive JUnit 5 tests using `@WebFluxTest`.
* **Mocking:** Used `Mockito` (`@MockitoBean`) to mock Service layers and isolate Controller logic.
* **Data Integrity:** Verified request validation (`@Valid`) and correct HTTP status codes.

---

## Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot 3 (WebFlux)
* **Database:** MongoDB (NoSQL)
* **Testing:** JUnit 5, Mockito, WebTestClient
* **Tools:** Maven, Lombok, Postman, JMeter

---

## 💾 Database Schema (NoSQL)

### Collections Overview

| Collection  | Description | Key Fields |
| :--- | :--- | :--- |
| **Airline** | Stores airline details. | `id`, `name`, `code` (Unique) |
| **Flight** | Stores schedule and inventory. | `id`, `airlineId`, `fromAirport`, `toAirport`, `price`, `availableSeats` |
| **User** | Stores customer info. | `id`, `name`, `email`, `role` |
| **Itinerary** | The main container for a trip (Group of bookings). | `id`, `pnr` (Unique), `userId`, `totalAmount`, `status` |
| **Booking** | Represents a single flight leg within an itinerary. | `id`, `itineraryId`, `flightId`, `segmentType` (OUTBOUND/RETURN) |
| **Passenger** | Individual passenger details for a booking. | `id`, `bookingId`, `name`, `seatNumber`, `mealType` |

### Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    AIRLINE ||--o{ FLIGHT : operates
    USER ||--o{ ITINERARY : books
    ITINERARY ||--|{ BOOKING : contains
    FLIGHT ||--o{ BOOKING : has
    BOOKING ||--|{ PASSENGER : includes

    FLIGHT {
        string id PK
        string airlineId FK
        string route
        int availableSeats
    }
    ITINERARY {
        string id PK
        string pnr
        string userId FK
    }
    BOOKING {
        string id PK
        string itineraryId FK
        string flightId FK
        enum segmentType
    }
