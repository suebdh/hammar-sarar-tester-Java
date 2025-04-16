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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    /**
     * Utility method to create a mock Ticket object for simulating a parked vehicle.
     * The ticket is set with a parking spot and a vehicle registration number.
     * The in-time is set to 1 hour ago (3600000 milliseconds).
     *
     * @author suebdh
     */
    private Ticket createMockTicket() {
        // Création d'un ticket cohérent pour simuler un client qui a garé son véhicule
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 3600000 = 60 * 60 * 1000 c.à.d il y a 1h
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(parkingSpot);
        return ticket;
    }

    @BeforeEach
    public void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            Ticket ticket = createMockTicket();//refactor : Ticket simulé via méthode utilitaire pour éviter répétitions : DRY

            //remove unnecessary stubbings
            //when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            //when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            //Injection des mocks dans la classe à tester (step2)
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {
        //corresponds precisely to processExitingVehicleTest_nonRegularClient_shouldNotApplyDiscount()
        // Arrange (Given)

        Ticket ticket = createMockTicket();//refactor : Ticket simulé via méthode utilitaire pour éviter répétitions : DRY
        //Définition ou Stubbing du comportement attendu du mock (stub)
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1); // 1 seul ticket → pas de remise

        // Act (When)
        parkingService.processExitingVehicle();

        // Assert (Then) : Vérifier que la méthode updateParking a bien été appelée
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        // Capturer l'argument passé à updateParking()
        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());

        // Vérifier que l'emplacement a bien été marqué comme disponible
        ParkingSpot capturedParkingSpot = parkingSpotCaptor.getValue();
        assertTrue(capturedParkingSpot.isAvailable(), "Le parking doit être marqué comme disponible");
    }

    /**
     * Test the behavior of processExitingVehicle() method when a regular client exits the parking.
     * The test ensures that the discount is applied when the client has more than one ticket.
     *
     * @author suebdh
     */
    @Test
    public void processExitingVehicleTest_RegularClient_shouldApplyDiscount() {
        // Arrange (Given) : Définir le comportement attendu du mock (stub)
        Ticket ticket = createMockTicket();//refactor : Ticket simulé via méthode utilitaire pour éviter répétitions : DRY

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(2); // + de 1 ticket → remise

        // Act (When)
        parkingService.processExitingVehicle();

        // Assert (Then) : Vérifier que la méthode updateParking a bien été appelée
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        // Capturer l'argument passé à updateParking()
        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());

        // Vérifier que l'emplacement a bien été marqué comme disponible
        ParkingSpot capturedParkingSpot = parkingSpotCaptor.getValue();
        assertTrue(capturedParkingSpot.isAvailable(), "Le parking doit être marqué comme disponible");
    }

    /**
     * Test the processIncomingVehicle() method of the ParkingService class.
     * This test simulates the process of a vehicle entering the parking lot,
     * ensuring that the vehicle's registration number is read, a parking spot is assigned,
     * and the ticket is saved correctly.
     *
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
    public void testProcessIncomingVehicle() //throws Exception
    {

        //Arrange (Given)
        // Ces deux stubbing ligne 113 et ligne 115 sont nécessaires pour simuler les entrées utilisateur avant que la méthode processIncomingVehicle() ne soit exécutée.
        // En effet, la méthode utilise ces valeurs (sélection d'une option et le numéro d'immatriculation du véhicule) pour déterminer les actions suivantes.
        // Le mock de `inputReaderUtil.readSelection()` simule la sélection d'une option par l'utilisateur,
        // et celui de `inputReaderUtil.readVehicleRegistrationNumber()` simule la saisie du numéro d'immatriculation.
        // Ils doivent être définis avant l'appel à `parkingSpotDAO.getNextAvailableSlot()` car ce dernier pourrait dépendre des informations d'entrée utilisateur pour déterminer la disponibilité du parking.

        when(inputReaderUtil.readSelection()).thenReturn(1);
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            throw new RuntimeException("Error mocking inputReaderUtil.readVehicleRegistrationNumber()", e);
        }
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        //On en a besoin car la méthode processIncomingVehicle() utilise ce résultat pour décider s’il y a une place libre.
        //when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");// pas besoin de la mettre, elle est déjà mise en place dans le setUp
        //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true); //on suppose que le premier spot est disponible
        // Utilisation de doReturn().when() au lieu de when().thenReturn() pour éviter une erreur de stubbing (argument mismatch avec any()).
        // Cela permet de mocker updateParking() sans exécuter la méthode réelle, et sans risquer de NullPointerException liée au matcher any().
        doReturn(true).when(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        // On en a besoin car cette méthode est appelée pour marquer la place comme occupée.
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        // Appelée pour savoir si une remise doit être appliquée ou non, ce qui est utile si on veut tester la logique de client régulier.
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        //Nécessaire pour vérifier que le ticket est bien enregistré.

        //Act (When)
        parkingService.processIncomingVehicle();

        //Assert(Then)
        // Vérification des comportements → que les bonnes méthodes ont bien été appelées
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));// Vérifie que l'emplacement suivant a été demandé
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));// Vérifie que l'emplacement a été marqué comme occupé
        verify(ticketDAO, times(1)).getNbTicket(anyString());// Vérifie que le nombre de tickets existants a été vérifié
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));// Vérifie que le ticket a été enregistré

        // Assertions : assert(expected, actual)
        // Vérification de l'état du parking (ex: disponibilité de l'emplacement)
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false); // parking spot should be occupied
        assertFalse(parkingSpot.isAvailable(), "L'emplacement de parking doit être marqué comme occupé.");
    }
}