# API Testing Examples

## Prerequisites
Make sure the application is running on `http://localhost:8080`

## Airlines Endpoints

### Get all airlines
```bash
curl -X GET http://localhost:8080/airlines
```

### Add a new airline
```bash
curl -X POST http://localhost:8080/airlines \
  -H "Content-Type: application/json" \
  -d '{
    "code": "VS",
    "name": "Virgin Atlantic",
    "country": "United Kingdom"
  }'
```

## Flights Endpoints

### Get flight by ID
```bash
curl -X GET http://localhost:8080/flights/1
```

### Search flights by route
```bash
curl -X GET "http://localhost:8080/flights/search?from=LHR&to=JFK"
```

### Add a new flight
```bash
curl -X POST http://localhost:8080/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "VS001",
    "airlineId": 1,
    "departureAirport": "LHR",
    "arrivalAirport": "JFK",
    "departureTime": "2024-12-01T10:00:00",
    "arrivalTime": "2024-12-01T18:00:00",
    "totalSeats": 300,
    "availableSeats": 300,
    "price": 599.99
  }'
```

## Passengers Endpoints

### Add a new passenger
```bash
curl -X POST http://localhost:8080/passengers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phone": "+1-555-123-4567",
    "passportNumber": "US123456789",
    "dateOfBirth": "1990-01-15"
  }'
```

## Bookings Endpoints

### Create a booking
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "passengerId": 1,
    "flightId": 1,
    "seatNumber": "12A"
  }'
```

### Get booking by ID
```bash
curl -X GET http://localhost:8080/bookings/1
```

### Cancel a booking
```bash
curl -X DELETE http://localhost:8080/bookings/1
```

### Get all bookings for a passenger
```bash
curl -X GET http://localhost:8080/passengers/1/bookings
```

## Expected Response Formats

### Airlines Response
```json
{
  "id": 1,
  "code": "EK",
  "name": "Emirates",
  "country": "United Arab Emirates",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### Flight Response
```json
{
  "id": 1,
  "flightNumber": "EK205",
  "airlineId": 1,
  "departureAirport": "DXB",
  "arrivalAirport": "LHR",
  "departureTime": "2024-12-01T10:00:00",
  "arrivalTime": "2024-12-01T17:00:00",
  "availableSeats": 50,
  "totalSeats": 380,
  "price": 899.99,
  "status": "SCHEDULED"
}
```

### Booking Response
```json
{
  "id": 1,
  "bookingReference": "EK20250101",
  "passengerId": 1,
  "flightId": 1,
  "seatNumber": "12A",
  "status": "CONFIRMED",
  "totalAmount": 899.99,
  "bookingDate": "2024-01-01T10:00:00"
}
```