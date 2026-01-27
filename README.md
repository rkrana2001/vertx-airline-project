# DVT's Airline Booking System - Vert.x Technical Assessment

## Overview

Welcome to the **Vert.x Reactive Programming Assessment**. This hands-on technical evaluation is designed to assess your expertise in building high-performance, scalable applications using modern reactive programming principles.

### What You'll Build
You'll develop a **complete RESTful API** for an international **Airline Booking System** that handles real-world scenarios including flight searches, passenger management, and booking operations across global airline networks.

### What We're Evaluating
This assessment measures your proficiency in:
- **Reactive Programming Patterns** - Mastery of non-blocking, event-driven architectures
- **Asynchronous Processing** - Proper use of Futures, Promises, and async flows
- **System Architecture** - Clean separation of concerns and modular design
- **Quality Engineering** - Comprehensive testing with enforced coverage thresholds
- **DevOps Practices** - Containerization and deployment readiness

### Why This Matters
In today's high-throughput, low-latency world, reactive programming isn't just a nice-to-have—it's essential. This assessment simulates real production challenges where:
- **Thousands of users** search for flights simultaneously
- **Database operations** must never block the event loop
- **System resilience** is critical for handling failures gracefully
- **Scalability** requirements demand efficient resource utilization

## Business Domain: Global Airline Booking System

### Domain Overview
You're building the backbone of a **global airline reservation platform** that connects travelers with flights operated by major international carriers. Think of systems powering platforms like Booking.com, Expedia, Kayak, or airline-specific booking engines.

### Core Entities & Relationships
The system orchestrates four interconnected business domains:

- **Airlines**: International carriers from diverse regions (Emirates, Qatar Airways, Ethiopian Airlines, Singapore Airlines, etc.)
- **Flights**: Scheduled routes connecting major global hubs with real-time availability and pricing
- **Passengers**: Traveler profiles with international documentation and preferences
- **Bookings**: Reservation transactions with unique references, seat assignments, and payment processing

### Real-World Complexity
Your implementation will handle authentic scenarios:
- **Multi-regional flight networks** spanning the Middle East, Africa, Asia, Europe, and Americas
- **Dynamic seat availability** that updates with each booking transaction
- **International passenger data** with diverse naming conventions and documentation
- **Booking lifecycle management** from reservation to cancellation

## Technology Stack & Requirements

### Mandatory Technology Stack
| Component           | Technology                 | Version  | Purpose                                       |
|---------------------|----------------------------|----------|-----------------------------------------------|
| **Language**        | Java (OpenJDK/Oracle)      | 21 LTS   | Modern language features & performance        |
| **Framework**       | Eclipse Vert.x             | 5.0.4+   | Reactive, non-blocking application platform   |
| **Database**        | H2 Database                | Embedded | Lightweight, fast in-memory SQL database      |
| **Database Client** | Vert.x Reactive SQL Client | 5.0.4+   | Non-blocking database interactions            |
| **Database Schema** | **PROVIDED SQL Files**     | Required | **MUST use provided schema.sql and data.sql** |
| **Testing**         | JUnit 5 + Vert.x Test      | Latest   | Async-aware testing with coverage enforcement |
| **Build System**    | Apache Maven               | 3.8+     | Dependency management & lifecycle             |
| **Coverage**        | JaCoCo                     | 0.8.12+  | Code coverage analysis & enforcement          |

### Critical Constraints
**FORBIDDEN TECHNOLOGIES**: 
- Spring Boot, Spring Framework (any variant)
- Blocking JDBC drivers (use Vert.x reactive clients only)
- Synchronous HTTP clients
- Thread.sleep() or any blocking operations

### Technical Excellence Standards

#### Reactive Programming Mastery
- **Event Loop Affinity**: All operations must be event-loop safe
- **Future Composition**: Proper chaining of async operations
- **Error Propagation**: Graceful handling in reactive streams
- **Backpressure Management**: Handle overwhelming request loads

#### Performance & Scalability
- **Non-blocking I/O**: Zero blocking operations in request handling
- **Resource Efficiency**: Minimal thread usage, maximum throughput
- **Memory Management**: Proper resource cleanup and leak prevention

#### Code Quality Standards
- **Separation of Concerns**: Clean architecture with distinct layers
- **SOLID Principles**: Well-structured, maintainable codebase
- **Error Handling**: Comprehensive exception management
- **Test Coverage**: Minimum 85% with meaningful assertions

## Database Schema

**IMPORTANT**: You **MUST use the provided database schema and seed data**. Do not create your own schema.

The database schema and seed data are already provided in:
- `src/main/resources/schema.sql` - Database tables and indexes
- `src/main/resources/data.sql` - Sample data from global airlines

**Your application must:**
- Load and execute the provided schema.sql on startup
- Load and execute the provided data.sql to populate initial data
- Use the exact table structure and relationships as defined

### Entities
1. **Airlines** - Airline companies (Emirates, Qatar Airways, Ethiopian Airlines, etc.)
2. **Flights** - Flight schedules with routes connecting different global regions
3. **Passengers** - Customer profiles with global diversity
4. **Bookings** - Ticket reservations with booking references

## REST API Endpoints to Implement

You must implement these **10 REST API endpoints** with proper JSON responses:

### Airlines
- `POST /airlines` → Add a new airline
- `GET /airlines` → List all airlines

### Flights  
- `POST /flights` → Add a new flight for an airline
- `GET /flights/{id}` → Get flight details by ID
- `GET /flights/search?from=X&to=Y` → Search flights by route

### Passengers
- `POST /passengers` → Add a passenger

### Bookings
- `POST /bookings` → Book a ticket for a passenger on a flight
- `GET /bookings/{id}` → Retrieve booking details
- `DELETE /bookings/{id}` → Cancel a booking
- `GET /passengers/{id}/bookings` → List all bookings of a passenger

### API Requirements
- All endpoints must return **JSON responses**
- Use proper HTTP status codes
- Implement proper error handling with meaningful error messages
- All database interactions must be **asynchronous and non-blocking**
- Use Vert.x Web Router for request routing

## Assessment Criteria

You will be evaluated on:

### 1. Reactive Programming
- Correct use of Vert.x Futures/Promises
- Proper async/await patterns
- Non-blocking request handling

### 2. Database Integration
- Correct use of Vert.x reactive SQL client
- Non-blocking database operations
- Proper connection management

### 3. Error Handling & Resilience
- Proper exception handling in async flows
- Meaningful error responses
- Backpressure management

### 4. Code Quality & Architecture
- Clean, readable code structure
- Proper separation of concerns (handlers, services, models)
- Effective use of DTOs and domain models

### 5. Testing
- Complete unit tests for all endpoints
- Proper use of Vert.x JUnit5 extension
- Test coverage of both success and error scenarios
- **Minimum 85% code coverage required** (build will fail if below a threshold)

### 6. Docker Containerization
- Complete Dockerfile implementation
- Proper multi-stage build (optional)
- Container runs successfully with the exposed port
- Documentation for Docker commands

## Project Structure

```
src/
├── main/
│   ├── java/com/airline/booking/
│   │   ├── MainVerticle.java           # Main application entry point
│   │   ├── model/                      # Domain entities
│   │   │   ├── Airline.java
│   │   │   ├── Flight.java
│   │   │   ├── Passenger.java
│   │   │   └── Booking.java
│   │   ├── dto/                        # Data Transfer Objects
│   │   ├── service/                    # Business logic services
│   │   │   └── DatabaseService.java
│   │   └── handler/                    # HTTP request handlers
│   │       └── AirlineHandler.java
│   └── resources/
│       ├── application.json            # Application configuration
│       ├── schema.sql                  # Database schema
│       └── data.sql                    # Sample data
├── test/
│   └── java/com/airline/booking/
│       └── AirlineHandlerTest.java     # Sample test structure
├── Dockerfile                          # Container definition
├── .dockerignore                       # Docker ignore patterns
└── pom.xml                            # Maven configuration
```

## Getting Started

### Prerequisites
- Java 21 (OpenJDK or Oracle JDK)
- Maven 3.8+

### Setup Instructions

1. **Clone the project**
   ```bash
   Clone your Git repository. Then navigate to project directory
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn exec:java
   # OR
   mvn compile exec:java -Dexec.mainClass="com.dvtsoftware.airline.booking.MainVerticle"
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

5. **Check test coverage**
   ```bash
   # Run tests with coverage report
   mvn clean verify
   
   # View coverage report
   open target/site/jacoco/index.html
   ```

6. **Verify the build passes the coverage threshold**
   ```bash
   # This will fail if coverage is below 85%
   mvn clean verify
   ```

### Development Tasks

Your implementation should cover:

1. **Domain Models**: Complete the entity classes with proper fields, constructors, getters/setters
2. **Database Service**: Implement reactive database operations using Vert.x SQL client
3. **HTTP Handlers**: Create request handlers for all 10 API endpoints
4. **Main Verticle**: Set up HTTP server, routing, and database initialization
5. **Error Handling**: Implement comprehensive error handling for all scenarios
6. **Unit Tests**: Write tests for all endpoints and core functionality
7. **Docker Containerization**: Complete the Dockerfile and containerize the application

### Sample API Usage

Once implemented, you should be able to test with curl. For more details refer to [api-examples.md](api-examples.md) file.

```bash
# Get all airlines
curl http://localhost:8080/airlines

# Add new airline
curl -X POST http://localhost:8080/airlines \
  -H "Content-Type: application/json" \
  -d '{"code":"VS","name":"Virgin Atlantic","country":"United Kingdom"}'

# Search flights
curl "http://localhost:8080/flights/search?from=LHR&to=JFK"

# Create booking
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerId":1,"flightId":1,"seatNumber":"12A"}'
```

## Database Configuration

The H2 database is configured in `application.json`:
- **URL**: `jdbc:h2:mem:airline_booking`
- **User**: `sa`
- **Password**: (empty)
- Database initializes automatically with schema and sample data

## Submission Guidelines

Your solution should demonstrate:
- Working REST API with all 10 endpoints
- Proper Vert.x reactive patterns
- Non-blocking database operations
- Comprehensive error handling
- Complete unit test suite with **a minimum 85% coverage**
- Clean, maintainable code architecture
- Successful `mvn clean verify` build (enforces a coverage threshold)

## Test Coverage Requirements

### Coverage Thresholds
The build is configured with **JaCoCo Maven plugin** to enforce minimum test coverage:

- **Line Coverage**: 85% minimum
- **Instruction Coverage**: 85% minimum  
- **Branch Coverage**: 80% minimum

### Coverage Enforcement
- The build will **FAIL** if coverage falls below these thresholds
- Run `mvn clean verify` to check coverage compliance
- Coverage reports are generated in `target/site/jacoco/index.html`

### Exclusions
The following are excluded from coverage requirements:
- Simple POJO model classes (`**/model/*`)
- Main method entry points

### Coverage Tips
- Focus on testing business logic, handlers, and service layers
- Test both success and error scenarios
- Use Vert.x test utilities for async testing
- Mock external dependencies appropriately

## Time Expectation
This assessment is designed to take approximately **4–6 hours** to complete for an experienced developer.

Focus on demonstrating your understanding of reactive programming principles and Vert.x framework capabilities rather than perfect UI or extensive business logic.

## Ready to Take Flight?

This assessment is more than a coding exercise. It is your opportunity to demonstrate mastery of modern reactive programming in a real-world context. You'll be working with the same technologies and patterns used by high-traffic platforms serving millions of users globally.

### Success Criteria Checklist
- [ ] All 10 REST endpoints are implemented and working
- [ ] Reactive patterns are used throughout (no blocking operations)
- [ ] 85%+ test coverage achieved
- [ ] `mvn clean verify` passes successfully
- [ ] Docker containerization completed
- [ ] Clean, maintainable code architecture
- [ ] No AI-generated code will be accepted 
- [ ] Plagiarism has not been detected in your submitted solution

### Final Reminders
- **Think Production-Ready**: Write code you'd be proud to deploy
- **Embrace Reactive**: Let Vert.x's event-driven nature guide your design
- **Test Thoroughly**: Your tests are as important as your implementation
- **Document Decisions**: Clear code tells a story

**Your mission**: Build a system that can handle a million flight bookings. Show us you're ready for the challenge!

**Happy Coding! Ready when you are. Let's build something amazing!**
