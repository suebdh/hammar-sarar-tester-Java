package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private Ticket ticket;
    private ParkingSpot parkingSpot;

    @BeforeEach
    public void setUpPerTest() {
        try {
            parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 3600000 = 60 * 60 * 1000 c.à.d il y a 1h
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            //Injection des mocks dans la classe à tester (step2)
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        //corresponds precisely to processExitingVehicleTest_nonRegularClient_shouldNotApplyDiscount()
        // Arrange (Given)
        // Definition or Stubbing of the expected behavior of the mock (stub)
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1); // 1 seul ticket → pas de remise
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Act (When)
        parkingService.processExitingVehicle();

        // Assert (Then) : // Verify that the updateParking method was called correctly
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        assertNotNull(ticket.getOutTime(), "The ticket out time must not be Null");
        assertTrue(parkingSpot.isAvailable(), "The parkingSpot must be marked as available after exiting the vehicle");
     }

    /**
     * Test the processIncomingVehicle() method of the ParkingService class.
     * This test simulates the process of a vehicle entering the parking lot,
     * ensuring that the vehicle's registration number is read, a parking spot is assigned,
     * and the ticket is saved correctly.
     * <p>
     * It mocks interactions with the input reader, parking spot DAO, and ticket DAO.
     * It checks if the following behaviors occur:
     * - The parking spot DAO is queried for the next available spot.
     * - The parking spot is updated to indicate it is now occupied.
     * - The ticket DAO is used to check the number of tickets and save the ticket.
     *
     * @throws Exception if any unexpected error occurs during the test execution.
     * @author suebdh
     */
    @Test
    public void testProcessIncomingVehicle() throws Exception {

        //Arrange (Given)
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        //Act (When)
        parkingService.processIncomingVehicle();

        //Assert(Then)
        // Verification of behaviors → ensuring that the correct methods were called
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));// Vérifie que l'emplacement a été marqué comme occupé
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));// Vérifie que le ticket a été enregistré
        verify(ticketDAO, times(1)).getNbTicket("ABCDEF");// Vérifie que le nombre de tickets existants a été vérifié

        // Assertions : assert(expected, actual)
        assertFalse(parkingSpot.isAvailable(), "L'emplacement de parking doit être marqué comme occupé.");
    }

    /**
     * Teste le comportement de la méthode processExitingVehicle() lorsque la mise à jour du ticket échoue.
     * La méthode updateTicket() retourne false, indiquant un échec de l'enregistrement.
     * Le test vérifie que, malgré l'échec, le parking n'est pas mis à jour (par exemple, pas libéré à tort).
     *
     * @author suebdh
     */
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // Arrange (Given)
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        // when(ticketDAO.getNbTicket(anyString())).thenReturn(1); // 1 seul ticket → pas de remise
        // Cette ligne est volontairement commentée car la mise à jour du ticket échoue (updateTicket() renvoie false),
        // Donc le traitement ne va pas jusqu'à l'étape où le nombre de tickets est utilisé pour déterminer si une remise s'applique.

        //Act (When)
        parkingService.processExitingVehicle();

        // Assert (Then) : // Verify that the updateParking method was NEVER called in case of updateTicket() failure
        verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));

        assertFalse(ticketDAO.updateTicket(any(Ticket.class)),"La mise à jour du ticket aurait dû échouer (updateTicket doit renvoyer false).");
    }

    /**
     * Teste la méthode getNextParkingNumberIfAvailable() pour s'assurer qu'elle retourne
     * un ParkingSpot avec l'identifiant 1 et qui est disponible.
     * <p>
     * Ce test simule une place disponible en retournant l'ID 1 depuis le mock DAO.
     * On vérifie que le ParkingSpot retourné correspond bien aux attentes.
     *
     * @author suebdh
     */
    @Test
    public void testGetNextParkingNumberIfAvailable() {
        //Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); //1= CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);//le num de la place de parking dispo est 1

        //Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        //Assert

        // Verify
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);

        assertTrue(parkingSpot != null, "Le ParkingSpot ne doit pas être null.");
        assertTrue(parkingSpot.getId() == 1, "L'identifiant du ParkingSpot doit être 1.");
        assertTrue(parkingSpot.getParkingType() == ParkingType.CAR, "Le type de Parking doit être CAR.");
        assertTrue(parkingSpot.isAvailable(), "Le ParkingSpot doit être disponible.");
    }

    /**
     * Teste le comportement de la méthode getNextParkingNumberIfAvailable()
     * lorsqu'aucune place de parking n'est disponible.
     * <p>
     * Le test vérifie que la méthode retourne null dans ce cas.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        //Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); //1= CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);//aucune place de parking n'est disponible
        //Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        //Assert
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);

        assertNull(parkingSpot, "Aucune place de parking ne doit être disponible (parkingSpot doit être null)");

    }

    // Teste la méthode getNextParkingNumberIfAvailable() avec une saisie invalide (3) pour le type de véhicule.
    // Vérifie que la méthode retourne null et que la méthode getNextAvailableSlot() n'est pas appelée.
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        //Arrange : Saisie invalide de l’utilisateur
        when(inputReaderUtil.readSelection()).thenReturn(3); // 3 : Valeur invalide : ni 1 (CAR) ni 2 (BIKE)

        //Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        //Assert

        verify(inputReaderUtil, times(1)).readSelection();
        // Verify : Vérifie que la méthode getNextAvailableSlot() n’a jamais été appelée, peu importe l’argument passé.
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any());
        assertNull(parkingSpot, "Aucune place de parking ne peut être affectée (Saisie type véhicule erroné)");
    }

    // Test: Verify behavior of getNextParkingNumberIfAvailable for BIKE
    // This test ensures that when the user selects BIKE (input = 2), the correct available parking spot is returned
    // It checks if the returned ParkingSpot is not null, has the correct ID, type, and availability status.
    @Test
    public void testGetNextParkingNumberIfAvailableForBIKE() {
        //Arrange
        when(inputReaderUtil.readSelection()).thenReturn(2); //2= BIKE
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);//le num de la place de parking dispo est 1

        //Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        //Assert
        assertTrue(parkingSpot != null, "Le ParkingSpot ne doit pas être null.");
        assertTrue(parkingSpot.getId() == 1, "L'identifiant du ParkingSpot doit être 1.");
        assertTrue(parkingSpot.getParkingType() == ParkingType.BIKE, "Le type de Parking doit être BIKE.");
        assertTrue(parkingSpot.isAvailable(), "Le ParkingSpot doit être disponible.");

        // Verify
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.BIKE);
    }

    @Test
    public void processIncomingVehicleException() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        // Forcer une exception ici pour tester le bloc catch de processIncomingVehicle().
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenThrow(new RuntimeException("Échec lors de la mise à jour de la disponibilité du parking"));

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, never()).saveTicket(any(Ticket.class));
    }
//La processExitingVehicle() gère correctement une exception qui se produit lors de la récupération d’un ticket
    @Test
    public void processExitingVehicleException() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenThrow(new RuntimeException("Impossible de récupérer le ticket associé à ce véhicule"));

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getTicket(anyString());
        verify(ticketDAO, never()).getNbTicket(anyString());
    }



}