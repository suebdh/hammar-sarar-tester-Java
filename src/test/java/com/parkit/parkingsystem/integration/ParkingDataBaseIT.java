package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.util.RoundUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    // Initialize test environment (DAOs and database config) before any test runs
    @BeforeAll
    public static void setUp() {
        // parkingSpotDAO: Object managing access to parking spots in the database Test
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        // ticketDAO: Object managing access to tickets in the database Test
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    // Prepare database state before each test (clean up + init ParkingService)
    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        // Reset the Test DB: Deleting tickets + All parking spots are available
        dataBasePrepareService.clearDataBaseEntries();
    }

    // Final clean-up after all tests (empty here but ready for future use)
    @AfterAll
    public static void tearDown() {
    }

    // Utility method to create a ticket and optionally set an exit time, updates ParkingSpot state
    private Ticket createTicket(boolean spotAvailability,
                                long inAgoMin,
                                Long durationMin) {

        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, spotAvailability);
        parkingSpotDAO.updateParking(spot);

        Date in = new Date(System.currentTimeMillis() - inAgoMin * 60_000);
        Date out = durationMin == null ? null
                : new Date(in.getTime() + durationMin * 60_000);

        Ticket t = new Ticket();
        t.setVehicleRegNumber("ABCDEF");
        t.setParkingSpot(spot);
        t.setInTime(in);
        t.setOutTime(out);
        t.setPrice(0);
        ticketDAO.saveTicket(t);
        return t;
    }

    // Tests that a vehicle entry creates a ticket and marks the parking spot as unavailable
    @Test
    public void testParkingACar() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Le ticket doit être enregistré");
        assertNotNull(ticket.getInTime(), "L'heure d'entrée du véhicule doit être renseigné");
        assertNull(ticket.getOutTime(), "L'heure de sortie doit rester vide à l'entrée du véhicule");
        assertEquals(0, ticket.getPrice(), "Le prix du ticket doit être égal à 0");

        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpotById(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot, "Le ParkingSpot récupéré ne doit pas être null");
        assertFalse(parkingSpot.isAvailable(), "L'emplacement parking ne doit pas être disponible");
    }

    // Tests vehicle exit and correct fare calculation after 60 minutes
    @Test
    public void testParkingLotExit() {
        // GIVEN – ticket ouvert depuis 60min
        createTicket(false, 60, null);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Le ticket ne peut être null");
        assertNotNull(ticket.getInTime(), "L'heure d'entrée doit être renseignée");
        assertNotNull(ticket.getOutTime(), "L'heure de sortie doit être renseignée");
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), "Attention, le prix calculé est erroné !");

        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpotById(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot, "Le ParkingSpot récupéré ne doit pas être null");
        assertTrue(parkingSpot.isAvailable(), "L'emplacement parking doit être disponible");
    }

    // Tests detection of recurring user and application of 5% discount on fare
    @Test
    public void testParkingLotExitRecurringUser() {
        // GIVEN
        // First pass yesterday: 30min, ticket closed, so the spot was freed (We leave the parking spot available to simulate that the vehicle has left the parking lot)
        createTicket(true, 24 * 60, 30L);
        // Second pass to simulate that the customer returned today: 60min, ticket opened
        createTicket(false, 60, null);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        // Verifies that there are 2 Tickets for the vehicle 'ABCDEF'
        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
        // Verification of the second Ticket
        Ticket ticketInDB = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketInDB);
        assertNotNull(ticketInDB.getInTime());
        assertNotNull(ticketInDB.getOutTime());
        assertEquals(RoundUtil.roundToTwoDecimals(Fare.CAR_RATE_PER_HOUR * 0.95), ticketInDB.getPrice());
        assertEquals(1, ticketInDB.getParkingSpot().getId());

        // Verification of ParkingSpot
        ParkingSpot parkingSpotInDB = parkingSpotDAO.getParkingSpotById(ticketInDB.getParkingSpot().getId());
        assertNotNull(parkingSpotInDB);
        assertTrue(parkingSpotInDB.isAvailable());
    }
}
