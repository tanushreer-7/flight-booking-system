import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightBookingSystem {
    private static final Scanner SC = new Scanner(System.in);
    private static final FlightRepository flightRepo = new FlightRepository();
    private static final BookingService bookingService = new BookingService();
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public static void main(String[] args) {
        flightRepo.seed();
        println("\n===== Flight Booking System =====\n");
        while (true) {
            try {
                println("1) List Flights\n2) View Flight & Seats\n3) Create Booking\n4) Pay for Booking\n5) View My Bookings\n6) Cancel Booking\n0) Exit");
                print("Choose: ");
                String ch = SC.nextLine().trim();
                switch (ch) {
                    case "1": listFlights(); break;
                    case "2": viewFlight(); break;
                    case "3": createBooking(); break;
                    case "4": payForBooking(); break;
                    case "5": viewBookings(); break;
                    case "6": cancelBooking(); break;
                    case "0": println("Thank you, Please visit again!"); return;
                    default: println("Invalid choice.\n");
                }
            } catch (Exception ex) {
                println("Error: " + ex.getMessage());
            }
        }
    }

    private static void listFlights() {
        println("\n-- Flights --");
        for (Flight f : flightRepo.findAll()) {
            println(f.summary());
        }
        println("");
    }

    private static void viewFlight() {
        print("Enter Flight ID: ");
        String id = SC.nextLine().trim();
        Flight f = flightRepo.findById(id);
        if (f == null) { println("Not found.\n"); return; }
        println("\n" + f.detailed());
        println("Seat Map (X = reserved):");
        f.printSeatMap();
        println("");
    }

    private static void createBooking() {
        print("Passenger Name: ");
        String name = SC.nextLine().trim();
        print("Passenger Email: ");
        String email = SC.nextLine().trim();
        Passenger p = new Passenger(name, email);

        print("Flight ID: ");
        String id = SC.nextLine().trim();
        Flight f = flightRepo.findById(id);
        if (f == null) { println("Flight not found.\n"); return; }

        println("Seat Map (X = reserved):");
        f.printSeatMap();
        print("Choose Seat (e.g., 1A): ");
        String seatNo = SC.nextLine().trim().toUpperCase();

        Booking b = bookingService.createBooking(f, seatNo, p);
        if (b == null) { println("Could not create booking.\n"); return; }
        println("\nBooking CREATED (pending payment)\n" + b.brief());
        println("");
    }

    private static void payForBooking() {
        print("Enter Booking ID: ");
        String id = SC.nextLine().trim();
        Booking b = bookingService.findById(id);
        if (b == null) { println("Booking not found.\n"); return; }

        println("\n-- Payment Methods --\n1) Card\n2) UPI\n3) Wallet");
        print("Choose method: ");
        String m = SC.nextLine().trim();
        switch (m) {
            case "1": println("Payment method: Card"); break;
            case "2": println("Payment method: UPI"); break;
            case "3": println("Payment method: Wallet"); break;
            default: println("Invalid."); return;
        }

        BigDecimal price = b.price;
        BigDecimal amount = null;
        while (true) {
            print("Enter payment amount (Ticket Price Rs." + price.toPlainString() + "): ");
            String amtStr = SC.nextLine().trim();
            try {
                amount = new BigDecimal(amtStr);
            } catch (NumberFormatException nfe) {
                println("Invalid amount. Please enter a number.");
                continue;
            }
            if (amount.compareTo(price) < 0) {
                println("Entered amount is less than ticket price. Please enter again.");
            } else {
                break;
            }
        }

        if (amount.compareTo(price) > 0) {
            BigDecimal change = amount.subtract(price);
            println("Payment accepted. Returning change Rs." + change.toPlainString());
        }

        print("Enter 4-digit card number: ");
        String cardNumber = SC.nextLine().trim();

        println("Payment in process... Please wait");
        try {
            int waitMs = 3000 + new java.util.Random().nextInt(2001);
            Thread.sleep(waitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        PaymentResult pr = bookingService.confirmManual(b.getId());
        println("Payment successful!");
        println("\nPayment Status: " + pr.status + (pr.txnId != null ? (" | TXN: " + pr.txnId) : ""));
        println(bookingService.findById(id).brief());
        println("");
    }

private static void viewBookings() {
    print("Enter passenger email to filter (blank for all): ");
    String email = SC.nextLine().trim();
    List<Booking> list = bookingService.findAllActive();
    if (!email.isEmpty()) {
        list.removeIf(b -> !b.getPassenger().email.equalsIgnoreCase(email));
    }
    if (list.isEmpty()) { 
        println("No bookings.\n"); 
        return; 
    }
    println("\n-- Bookings --");
    for (Booking b : list) println(b.brief());
    println("");
}

private static void cancelBooking() {
    if (bookingService.findAllActive().isEmpty()) {
        println("No bookings to cancel.\n");
        return;
    }

    print("Enter Booking ID: ");
    String id = SC.nextLine().trim();
    BigDecimal refund = bookingService.cancel(id);
    if (refund != null) {
        println("Booking cancelled. Refund Amount: Rs." + refund.toPlainString());
    } else {
        println("Unable to cancel.");
    }
    println("");
}


    private static void println(String s) { System.out.println(s); }
    private static void print(String s) { System.out.print(s); }
}


class FlightRepository {
    private final Map<String, Flight> flights = new LinkedHashMap<>();

    public void seed() {
        flights.clear();
        flights.put("AI101", Flight.sample("AI101", "Air India", "DEL", "BLR", LocalDateTime.now().plusDays(1).withHour(9).withMinute(25), new BigDecimal("6500")));
        flights.put("6E202", Flight.sample("6E202", "IndiGo", "BOM", "DEL", LocalDateTime.now().plusDays(2).withHour(14).withMinute(10), new BigDecimal("4200")));
        flights.put("UK303", Flight.sample("UK303", "Vistara", "BLR", "GOI", LocalDateTime.now().plusDays(3).withHour(7).withMinute(45), new BigDecimal("3800")));
    }

    public List<Flight> findAll() { return new ArrayList<>(flights.values()); }
    public Flight findById(String id) { return flights.get(id); }
}

class Flight {
    final String id, airline, origin, destination;
    final LocalDateTime departure;
    final BigDecimal baseFare;
    final Map<String, Seat> seats;

    Flight(String id, String airline, String origin, String destination, LocalDateTime departure, BigDecimal baseFare, Map<String, Seat> seats) {
        this.id = id; this.airline = airline; this.origin = origin; this.destination = destination;
        this.departure = departure; this.baseFare = baseFare; this.seats = seats;
    }

    static Flight sample(String id, String airline, String from, String to, LocalDateTime time, BigDecimal baseFare) {
        Map<String, Seat> map = new LinkedHashMap<>();
        for (int r = 1; r <= 6; r++) {
            for (char c = 'A'; c <= 'D'; c++) {
                String seatNo = r + String.valueOf(c);
                SeatClass sc = (r <= 2) ? SeatClass.BUSINESS : SeatClass.ECONOMY;
                BigDecimal mult = (sc == SeatClass.BUSINESS) ? new BigDecimal("1.8") : BigDecimal.ONE;
                map.put(seatNo, new Seat(seatNo, sc, mult));
            }
        }
        return new Flight(id, airline, from, to, time, baseFare, map);
    }

    synchronized boolean reserve(String seatNo) {
        Seat s = seats.get(seatNo);
        if (s == null || s.reserved) return false;
        s.reserved = true;
        return true;
    }

    synchronized void release(String seatNo) {
        Seat s = seats.get(seatNo);
        if (s != null) s.reserved = false;
    }

    List<Seat> availableSeats() {
        List<Seat> list = new ArrayList<>(seats.values());
        list.removeIf(s -> s.reserved);
        return list;
    }

    BigDecimal priceFor(String seatNo) {
        Seat s = seats.get(seatNo);
        if (s == null) return null;
        return baseFare.multiply(s.priceMultiplier).setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    String summary() {
        return String.format(Locale.US, "%s | %-8s | %s -> %s | %s | From Rs.%s | Avail seats: %d", id, airline, origin, destination,
                departure.format(DateTimeFormatter.ofPattern("dd-MMM HH:mm")), baseFare.toPlainString(), availableSeats().size());
    }

    String detailed() {
        return String.format("Flight %s (%s) %s -> %s\nDeparts: %s\nBase Fare: Rs.%s\nBusiness multiplier 1.8x, Economy 1.0x", id, airline, origin, destination,
                departure.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")), baseFare.toPlainString());
    }

    void printSeatMap() {
        for (int r = 1; r <= 6; r++) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("%2d ", r));
            for (char c = 'A'; c <= 'D'; c++) {
                String sn = r + String.valueOf(c);
                Seat s = seats.get(sn);
                row.append(s.reserved ? " X " : (" " + c + " "));
                if (c == 'B') row.append("  ");
            }
            System.out.println(row);
        }
        System.out.println("    A B   C D\n");
    }
}

enum SeatClass { ECONOMY, BUSINESS }

class Seat {
    final String seatNo; final SeatClass seatClass; final BigDecimal priceMultiplier; boolean reserved;
    Seat(String seatNo, SeatClass seatClass, BigDecimal priceMultiplier) {
        this.seatNo = seatNo; this.seatClass = seatClass; this.priceMultiplier = priceMultiplier; this.reserved = false;
    }
}

class Passenger {
    final String name; final String email;
    Passenger(String name, String email) { this.name = name; this.email = email; }
}

enum BookingStatus { PENDING_PAYMENT, CONFIRMED, CANCELLED }

enum PaymentStatus { SUCCESS, FAILED }

class Booking {
    final String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    final Flight flight; final Passenger passenger; final String seatNo;
    final BigDecimal price;
    BookingStatus status = BookingStatus.PENDING_PAYMENT;
    PaymentStatus lastPaymentStatus = null; String lastTxnId = null;

    Booking(Flight flight, Passenger passenger, String seatNo, BigDecimal price) {
        this.flight = flight; this.passenger = passenger; this.seatNo = seatNo; this.price = price;
    }

    String getId() { return id; }
    Passenger getPassenger() { return passenger; }

    String brief() {
        return String.format(Locale.US,
            "Booking %s | %s %s->%s %s | Seat %s | Price Rs.%s | %s",
            id, flight.id, flight.origin, flight.destination,
            flight.departure.format(DateTimeFormatter.ofPattern("dd-MMM HH:mm")),
            seatNo, price.toPlainString(), status);
    }
}

interface PaymentProcessor {
    PaymentResult charge(BigDecimal amount, String narrative);
    String name();
}

class PaymentResult {
    final PaymentStatus status; final String txnId;
    PaymentResult(PaymentStatus s, String id) { this.status = s; this.txnId = id; }
}

class CardPaymentProcessor implements PaymentProcessor {
    private final Random rand = new Random();
    public PaymentResult charge(BigDecimal amount, String narrative) {
        boolean ok = rand.nextDouble() > 0.1;
        return new PaymentResult(ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, ok ? ("CARD"+System.currentTimeMillis()) : null);
    }
    public String name() { return "Card"; }
}

class UpiPaymentProcessor implements PaymentProcessor {
    private final Random rand = new Random();
    public PaymentResult charge(BigDecimal amount, String narrative) {
        boolean ok = rand.nextDouble() > 0.08;
        return new PaymentResult(ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, ok ? ("UPI"+System.currentTimeMillis()) : null);
    }
    public String name() { return "UPI"; }
}

class WalletPaymentProcessor implements PaymentProcessor {
    private final Random rand = new Random();
    public PaymentResult charge(BigDecimal amount, String narrative) {
        boolean ok = rand.nextDouble() > 0.2; // 80% success
        return new PaymentResult(ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, ok ? ("WAL"+System.currentTimeMillis()) : null);
    }
    public String name() { return "Wallet"; }
}

class BookingService {
    private final Map<String, Booking> store = new LinkedHashMap<>();

    public Booking createBooking(Flight flight, String seatNo, Passenger p) {
        if (flight == null || seatNo == null) return null;
        synchronized (flight) {
            if (!flight.reserve(seatNo)) {
                System.out.println("Seat unavailable. Choose another.");
                return null;
            }
        }
        BigDecimal price = flight.priceFor(seatNo);
        if (price == null) {
            flight.release(seatNo);
            System.out.println("Seat does not exist.");
            return null;
        }
        Booking b = new Booking(flight, p, seatNo, price);
        store.put(b.getId(), b);
        return b;
    }

    public PaymentResult confirmManual(String bookingId) {
        Booking b = store.get(bookingId);
        if (b == null || b.status == BookingStatus.CANCELLED) {
            return new PaymentResult(PaymentStatus.FAILED, null);
        }
        b.status = BookingStatus.CONFIRMED;
        b.lastPaymentStatus = PaymentStatus.SUCCESS;
        b.lastTxnId = "USER" + System.currentTimeMillis();
        return new PaymentResult(PaymentStatus.SUCCESS, b.lastTxnId);
    }

    public BigDecimal cancel(String bookingId) {
        Booking b = store.get(bookingId);
        if (b == null) return null;
        if (b.status == BookingStatus.CANCELLED) return null;

        boolean refundable = b.status != BookingStatus.CONFIRMED 
                             || b.flight.departure.isAfter(LocalDateTime.now().plusHours(24));
        if (!refundable) return null;

        b.status = BookingStatus.CANCELLED;
        b.flight.release(b.seatNo);

        store.remove(b.getId());

        return b.price.multiply(new BigDecimal("0.40"))
                      .setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    public Booking findById(String id) { return store.get(id); }

    public List<Booking> findAllActive() {
        List<Booking> list = new ArrayList<>();
        for (Booking b : store.values()) {
            if (b.status != BookingStatus.CANCELLED) list.add(b);
        }
        return list;
    }

    public List<Booking> findAll() { return new ArrayList<>(store.values()); }
}
