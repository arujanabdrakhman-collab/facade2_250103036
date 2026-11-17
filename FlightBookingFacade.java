public class FlightBookingFacade {
    // Encapsulated subsystem references
    private final SeatInventorySystem seatSystem;
    private final BaggagePricingEngine baggageEngine;
    private final CurrencyConverter currencyConverter;
    private final PaymentProcessor paymentProcessor;
    private final TicketIssuer ticketIssuer;
    public FlightBookingFacade() {
        this.seatSystem = new SeatInventorySystem();
        this.baggageEngine = new BaggagePricingEngine();
        this.currencyConverter = new CurrencyConverter();
        this.paymentProcessor = new PaymentProcessor();
        this.ticketIssuer = new TicketIssuer();
    }
/**
* Single entry point for booking a complete flight itinerary.
* Coordinates seat locking, baggage pricing, currency conversion,
            * payment execution, rollback on failure, and ticket issuance.
    */
    public String bookFlight(String passengerName,
                             String flightCode,
                             String seatNumber,
                             double baseFareUSD,
                             int bagCount,
                             double bagWeightKg,
                             String currency,
                             String cardNumber) {
        String lockToken = null;
        try {
            seatSystem.connectToReservationServer();
            lockToken=seatSystem.lockSeat(flightCode, seatNumber,passengerName);
            double baggageFee=baggageEngine.calculateBaggageFeeUSD(bagCount, bagWeightKg);
            double totalUsd=baseFareUSD+baggageFee;
            double converteTotal= currencyConverter.convertFromUSD(totalUsd, currency);
            paymentProcessor.initializeGateway();
            String receiptID=paymentProcessor.chargeCard(cardNumber, converteTotal, currency);

// TODO 1: Connect to seat server and lock the requested seat.
// TODO 2: Compute baggage fee in USD and add to baseFareUSD.
// TODO 3: Convert total USD amount into target currency.
// TODO 4: Initialize payment gateway and charge the card.
// TODO 5: Generate and return the final e-ticket code.
            return ticketIssuer.generateTicket(passengerName, flightCode, lockToken, receiptID );
        } catch (RuntimeException ex) {
            if(lockToken!=null){
                seatSystem.releaseSeatLock(lockToken);
            }
            System.out.println("booking failed:" +ex.getMessage());

// TODO 6: If a seat was already locked, release it before reporting failure.
            return "BOOKING_FAILED: " + ex.getMessage();
        } finally {
            seatSystem.disconnect();
// TODO 7: Ensure seat server disconnects regardless of success or failure.
        }
    }
}