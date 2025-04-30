package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.RoundUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(RoundUtil.roundToTwoDecimals(Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareBike() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(RoundUtil.roundToTwoDecimals(Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareUnkownType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(RoundUtil.roundToTwoDecimals(0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(RoundUtil.roundToTwoDecimals(0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(RoundUtil.roundToTwoDecimals(24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        Date outTime = new Date();
        outTime.setTime(inTime.getTime() + (29 * 60 * 1000));  // 29 minutes after inTime

        // Create a ParkingSpot object for a car
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        // Create a Ticket and set its values
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Call the calculateFare method
        fareCalculatorService.calculateFare(ticket);

        // Check that the price is 0 for parking under 30 minutes for a car
        assertEquals(0, ticket.getPrice(), "The fare should be 0 for less than 30 minutes of parking time of a CAR.");
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        Date outTime = new Date();
        outTime.setTime(inTime.getTime() + (29 * 60 * 1000));  // 29 minutes after inTime

        // Create a ParkingSpot object for a bike
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        // Create a Ticket and set its values
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Call the calculateFare method
        fareCalculatorService.calculateFare(ticket);

        // Check that the price is 0 for parking under 30 minutes for a bike
        assertEquals(0, ticket.getPrice(), "The fare should be 0 for less than 30 minutes of parking time of a BIKE.");
    }

    /**
     * BONUS – Additional test to verify fare calculation for exactly 30 minutes and above.
     * This test was not explicitly required in step 3 of the project.
     */
    @Test
    public void calculateFareCarWithExactly30Minutes_shouldApplyHalfRate() {
        // Arrange
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + 30 * 60 * 1000); // 30 min
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Act
        fareCalculatorService.calculateFare(ticket);

        // Assert
        double expectedFare = Fare.CAR_RATE_PER_HOUR * 0.5;
        assertEquals(expectedFare, ticket.getPrice(), "Fare should be 50% of hourly rate for 30 minutes");
    }

    /**
     * BONUS – Additional test to verify fare calculation for more than 30 minutes.
     * This test was not explicitly required in step 3 of the project.
     */
    @Test
    public void calculateFareCarWithMoreThan30Minutes_shouldApplyCorrectRate() {
        // Arrange
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + 45 * 60 * 1000); // 45 min
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Act
        fareCalculatorService.calculateFare(ticket);

        // Assert
        double expectedFare = Fare.CAR_RATE_PER_HOUR * 0.75;
        assertEquals(RoundUtil.roundToTwoDecimals(expectedFare), ticket.getPrice(), "Fare should be 75% of hourly rate for 45 minutes");
    }

    @Test
    public void calculateFareCarWithDiscountDescription() {
        // Arrange
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + 60 * 60 * 1000); // 60 min
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Act
        fareCalculatorService.calculateFare(ticket, true);

        //Assert
        assertEquals(RoundUtil.roundToTwoDecimals(Fare.CAR_RATE_PER_HOUR * Fare.RATE_AFTER_REDUCTION), ticket.getPrice(), "Expected Reduction 95% of the full price for the CAR, price obtained = " + ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscountDescription() {
        // Arrange
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + 60 * 60 * 1000); // 60 min
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Act
        fareCalculatorService.calculateFare(ticket, true);

        //Assert
        assertEquals(RoundUtil.roundToTwoDecimals(Fare.BIKE_RATE_PER_HOUR * Fare.RATE_AFTER_REDUCTION), ticket.getPrice(), "Expected Reduction 95% of the full price for the MOTO, price obtained = " + ticket.getPrice());

    }
}