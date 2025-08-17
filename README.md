# ✈️ Flight Booking System (Java)

This is a **console-based Flight Booking System** implemented in **Java** as part of my **Pinnacle Labs Internship (Task 1)**.  
It simulates how a basic backend for a booking system works – from listing flights to booking seats, handling payments, and even cancellations with refunds.

---

## 🚀 Features

- **Flight Listing**
  - View all available flights with details (airline, origin, destination, price, available seats).
- **View Flight Details & Seat Map**
  - Check flight details and seat availability (reserved seats marked).
- **Create Booking**
  - Enter passenger details and reserve seats.
- **Payment Simulation**
  - Supports Card, UPI, Wallet (simulation only).
  - If a user pays more than the ticket price, the balance is returned.
- **Booking Management**
  - View bookings (all or filter by passenger email).
- **Cancellation & Refund**
  - Cancel confirmed bookings and receive an **automated 40% refund**.
  - Cancelled seats are released back to availability.

---

## 🛠️ Tech Stack

- **Java SE** (Core Java)
- **OOP Principles** (Encapsulation, Inheritance, Polymorphism)
- **Collections Framework**
- **BigDecimal** for currency handling
- **Console-based UI**

---

## 📂 Project Structure

- `FlightBookingSystem.java` → Main entry point with menu-driven console.
- `Flight`, `Seat`, `Passenger` → Core domain models.
- `Booking`, `BookingService` → Booking and service layer.
- `PaymentProcessor` (Card, UPI, Wallet) → Simulated payment handling.

---

## ⚡ How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/flight-booking-system.git
   cd flight-booking-system
2. Compile the program:
   ```bash
   javac FlightBookingSystem.java 
3. Run the program:
   ```bash
   java FlightBookingSystem

---

##🎮 Sample Usage:

===== Flight Booking System =====

1) List Flights
2) View Flight & Seats
3) Create Booking
4) Pay for Booking
5) View My Bookings
6) Cancel Booking
0) Exit

---

##🙌 Acknowledgement

This project was built as part of my internship at Pinnacle Labs to strengthen my Java and backend development skills.
