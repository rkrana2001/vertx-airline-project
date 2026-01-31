-- Seed Data for Airline Booking System
-- Sample data for technical assessment

-- Insert Airlines (Global representation: Middle East, Africa, Asia, Europe, Americas)
INSERT INTO airlines (code, name, country) VALUES 
-- Middle Eastern Airlines
('EK', 'Emirates', 'United Arab Emirates'),
('QR', 'Qatar Airways', 'Qatar'),
('EY', 'Etihad Airways', 'United Arab Emirates'),
('RJ', 'Royal Jordanian', 'Jordan'),
('MS', 'EgyptAir', 'Egypt'),
-- African Airlines
('ET', 'Ethiopian Airlines', 'Ethiopia'),
('SAA', 'South African Airways', 'South Africa'),
('KQ', 'Kenya Airways', 'Kenya'),
('AT', 'Royal Air Maroc', 'Morocco'),
-- Asian Airlines
('SQ', 'Singapore Airlines', 'Singapore'),
('CX', 'Cathay Pacific', 'Hong Kong'),
('TG', 'Thai Airways', 'Thailand'),
('MH', 'Malaysia Airlines', 'Malaysia'),
('AI', 'Air India', 'India'),
('JL', 'Japan Airlines', 'Japan'),
-- European Airlines
('BA', 'British Airways', 'United Kingdom'),
('LH', 'Lufthansa', 'Germany'),
('AF', 'Air France', 'France'),
('KL', 'KLM Royal Dutch Airlines', 'Netherlands'),
('AZ', 'Alitalia', 'Italy'),
('IB', 'Iberia', 'Spain');

-- Insert Passengers (Global diversity)
INSERT INTO passengers (first_name, last_name, email, phone, passport_number, date_of_birth) VALUES 
('Ahmed', 'Al-Rashid', 'ahmed.alrashid@email.com', '+971-50-123-4567', 'AE987654321', '1985-06-15'),
('Fatima', 'Hassan', 'fatima.hassan@email.com', '+974-5555-0124', 'QA123456789', '1990-03-22'),
('Kofi', 'Asante', 'kofi.asante@email.com', '+233-20-123-4567', 'GH456789123', '1982-11-08'),
('Priya', 'Sharma', 'priya.sharma@email.com', '+91-98-765-43210', 'IN789123456', '1988-01-30'),
('Takeshi', 'Yamamoto', 'takeshi.yamamoto@email.com', '+81-90-1234-5678', 'JP321654987', '1975-09-12'),
('Amara', 'Okafor', 'amara.okafor@email.com', '+234-802-555-0125', 'NG555666777', '1992-07-18'),
('Chen', 'Wei', 'chen.wei@email.com', '+852-9876-5432', 'HK111222333', '1987-04-25'),
('Nalini', 'Patel', 'nalini.patel@email.com', '+65-8765-4321', 'SG444555666', '1983-12-10');

-- Insert Flights (next 30 days from current date) - Global routes
INSERT INTO flights (flight_number, airline_id, departure_airport, arrival_airport, departure_time, arrival_time, available_seats, total_seats, price, status) VALUES 
-- Emirates flights (Middle East)
('EK205', 1, 'DXB', 'LHR', DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 7, DATEADD('DAY', 1, CURRENT_TIMESTAMP)), 50, 380, 899.99, 'SCHEDULED'),
('EK206', 1, 'LHR', 'DXB', DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 6, DATEADD('DAY', 1, CURRENT_TIMESTAMP)), 75, 380, 949.99, 'SCHEDULED'),
('EK231', 1, 'DXB', 'SIN', DATEADD('DAY', 2, CURRENT_TIMESTAMP), DATEADD('HOUR', 7, DATEADD('DAY', 2, CURRENT_TIMESTAMP)), 45, 350, 699.99, 'SCHEDULED'),

-- Qatar Airways flights (Middle East)
('QR100', 2, 'DOH', 'LHR', DATEADD('DAY', 3, CURRENT_TIMESTAMP), DATEADD('HOUR', 6, DATEADD('DAY', 3, CURRENT_TIMESTAMP)), 60, 350, 849.99, 'SCHEDULED'),
('QR101', 2, 'LHR', 'DOH', DATEADD('DAY', 3, CURRENT_TIMESTAMP), DATEADD('HOUR', 6, DATEADD('DAY', 3, CURRENT_TIMESTAMP)), 30, 350, 879.99, 'SCHEDULED'),
('QR920', 2, 'DOH', 'ADD', DATEADD('DAY', 4, CURRENT_TIMESTAMP), DATEADD('HOUR', 4, DATEADD('DAY', 4, CURRENT_TIMESTAMP)), 80, 300, 599.99, 'SCHEDULED'),

-- Ethiopian Airlines flights (Africa)
('ET500', 6, 'ADD', 'LHR', DATEADD('DAY', 5, CURRENT_TIMESTAMP), DATEADD('HOUR', 7, DATEADD('DAY', 5, CURRENT_TIMESTAMP)), 40, 280, 749.99, 'SCHEDULED'),
('ET501', 6, 'LHR', 'ADD', DATEADD('DAY', 5, CURRENT_TIMESTAMP), DATEADD('HOUR', 8, DATEADD('DAY', 5, CURRENT_TIMESTAMP)), 25, 280, 799.99, 'SCHEDULED'),
('ET302', 6, 'ADD', 'NBO', DATEADD('DAY', 6, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 6, CURRENT_TIMESTAMP)), 90, 180, 299.99, 'SCHEDULED'),

-- Kenya Airways flights (Africa)
('KQ100', 8, 'NBO', 'CDG', DATEADD('DAY', 7, CURRENT_TIMESTAMP), DATEADD('HOUR', 8, DATEADD('DAY', 7, CURRENT_TIMESTAMP)), 35, 250, 799.99, 'SCHEDULED'),
('KQ101', 8, 'CDG', 'NBO', DATEADD('DAY', 7, CURRENT_TIMESTAMP), DATEADD('HOUR', 9, DATEADD('DAY', 7, CURRENT_TIMESTAMP)), 55, 250, 829.99, 'SCHEDULED'),

-- Singapore Airlines flights (Asia)
('SQ317', 11, 'SIN', 'LHR', DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 13, DATEADD('DAY', 10, CURRENT_TIMESTAMP)), 20, 350, 1299.99, 'SCHEDULED'),
('SQ318', 11, 'LHR', 'SIN', DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 13, DATEADD('DAY', 10, CURRENT_TIMESTAMP)), 15, 350, 1399.99, 'SCHEDULED'),
('SQ423', 11, 'SIN', 'NRT', DATEADD('DAY', 12, CURRENT_TIMESTAMP), DATEADD('HOUR', 7, DATEADD('DAY', 12, CURRENT_TIMESTAMP)), 70, 300, 599.99, 'SCHEDULED'),

-- Cathay Pacific flights (Asia)
('CX252', 12, 'HKG', 'LHR', DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 12, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 30, 350, 1199.99, 'SCHEDULED'),
('CX253', 12, 'LHR', 'HKG', DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 11, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 40, 350, 1249.99, 'SCHEDULED'),

-- Air India flights (Asia)
('AI131', 15, 'DEL', 'LHR', DATEADD('DAY', 16, CURRENT_TIMESTAMP), DATEADD('HOUR', 9, DATEADD('DAY', 16, CURRENT_TIMESTAMP)), 45, 300, 699.99, 'SCHEDULED'),
('AI132', 15, 'LHR', 'DEL', DATEADD('DAY', 16, CURRENT_TIMESTAMP), DATEADD('HOUR', 8, DATEADD('DAY', 16, CURRENT_TIMESTAMP)), 50, 300, 749.99, 'SCHEDULED'),

-- British Airways flights (Europe)
('BA157', 17, 'LHR', 'FRA', DATEADD('DAY', 18, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, DATEADD('DAY', 18, CURRENT_TIMESTAMP)), 80, 180, 199.99, 'SCHEDULED'),
('BA158', 17, 'FRA', 'LHR', DATEADD('DAY', 18, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, DATEADD('DAY', 18, CURRENT_TIMESTAMP)), 75, 180, 229.99, 'SCHEDULED'),

-- Lufthansa flights (Europe)
('LH400', 18, 'FRA', 'CDG', DATEADD('DAY', 21, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, DATEADD('DAY', 21, CURRENT_TIMESTAMP)), 85, 150, 149.99, 'SCHEDULED'),
('LH401', 18, 'CDG', 'FRA', DATEADD('DAY', 21, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, DATEADD('DAY', 21, CURRENT_TIMESTAMP)), 70, 150, 179.99, 'SCHEDULED'),

-- KLM flights (Europe)
('KL123', 20, 'AMS', 'JFK', DATEADD('DAY', 25, CURRENT_TIMESTAMP), DATEADD('HOUR', 8, DATEADD('DAY', 25, CURRENT_TIMESTAMP)), 35, 280, 699.99, 'SCHEDULED'),
('KL124', 20, 'JFK', 'AMS', DATEADD('DAY', 25, CURRENT_TIMESTAMP), DATEADD('HOUR', 7, DATEADD('DAY', 25, CURRENT_TIMESTAMP)), 40, 280, 729.99, 'SCHEDULED');

-- Insert Sample Bookings
INSERT INTO bookings (booking_reference, passenger_id, flight_id, seat_number, status, total_amount) VALUES 
('EK20250101', 1, 1, '12A', 'CONFIRMED', 899.99),
('QR20250102', 2, 4, '15C', 'CONFIRMED', 849.99),
('ET20250103', 3, 7, '8B', 'CONFIRMED', 749.99),
('KQ20250104', 4, 10, '22D', 'CONFIRMED', 799.99),
('SQ20250105', 5, 13, '5A', 'CONFIRMED', 1299.99),
('CX20250106', 6, 16, '18F', 'CONFIRMED', 1199.99),
('AI20250107', 7, 18, '11B', 'CONFIRMED', 699.99),
('BA20250108', 8, 20, '25E', 'CONFIRMED', 199.99);

-- Update available seats count after bookings
UPDATE flights SET available_seats = available_seats - 1 WHERE id IN (1, 4, 7, 10, 13, 16, 18, 20);
