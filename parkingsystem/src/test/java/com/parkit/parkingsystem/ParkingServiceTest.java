
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
    }

//  EXEMPLE :
//    mock utilisateur qui saisit 1 pour une voiture
//    mock la sauvegarde du ticket (méthode saveTicket, alors return true)
//    mock renvoyer 1 pour la place de parking
//    mock nbTicket pour qu'il renvoie 0 (jamais venu) 
//    packingService.processIncomingVehicle();
//    vérifie que le parking a été updaté 1 fois (classe ParkingSpotDAO)
//    vérifie que le ticket a été enregistré 1 fois (classe TicketDAO)
//    vérifie que nbTicket a été utilisée 1 fois
    
    @Test
    public void testProcessIncomingVehicle() {

   //Mock utilisateur pour sélectionner une voiture (1)
   when(inputReaderUtil.readSelection()).thenReturn(1);

   //Mock de la sauvegarde du ticket
   when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

   //Mock renvoyant 1 pour la place de parking
   when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

   //Mock renvoyant 0 pour nbTicket (jamais venu)
    when(ticketDAO.getNbTicket(anyString())).thenReturn(0);

   //Appel de la méthode à tester
   parkingService.processIncomingVehicle();

   // Vérification que le parking a été mis à jour 1 fois 
   verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

   // Vérification que le ticket a été enregistré 1 fois 
   verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

   // Vérification que nbTicket a été utilisé 1 fois
   verify(ticketDAO, times(1)).getNbTicket(anyString());
}


@Test
public void processExitingVehicleTestUnableUpdate() {
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));

    // Ticket ticket = new Ticket(); // pourtant éditer un nouveau ticket est déja réalisé en @beforeEach mais si je l'enleve la variable ticket n'est pas définie
    
//     //Mock méthode getTicket() est appelée (any string) et renvoie l'objet ticket
//     when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

//     //Mock méthode updateTicket() est appelée avec l'objet ticket en argument et renvoie false


//     //Appel de la méthode à tester
//     parkingService.processExitingVehicle();

//     //Vérification getTicket() a été appelée une fois avec n'importe quelle chaîne de caractères
//     verify(ticketDAO, times(1)).getTicket(anyString());

//     //Vérification updateTicket() a été appelée une fois
//     verify(ticketDAO, times(1)).updateTicket(ticket);

//     //Vérifie que la méthode println() de System.out a été appelée une fois avec la chaîne de caractères spécifiée en argument ("Unable to update ticket information. Error occurred")
//     verify(System.out, times(1)).println("Unable to update ticket information. Error occurred");
// 
}


@Test
public void testGetNextParkingNumberIfAvailable() {

    when(inputReaderUtil.readSelection()).thenReturn(1);

    // Crée un objet ParkingSpot avec ID=1 et disponibilité=true
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

    // Mock méthode getNextAvailableSlot() pour renvoyer le parkingSpot créé
    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(parkingSpot.getId());

    // Appel de la méthode à tester
    ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

    // Vérifie que la méthode getNextAvailableSlot() est appelée avec le bon type de véhicule
    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));

    // Vérifie que le résultat est parkingSpot avec ID=1 et disponibilité=true
    assertNotNull(result);

    assertEquals(1, result.getId());

    assertTrue(result.isAvailable());
}


@Test
public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

    when(inputReaderUtil.readSelection()).thenReturn(1);

    // Mock la méthode getNextAvailableSlot() pour renvoyer null
    when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

    // Appel de la méthode à tester
    ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

    // Vérifie que getNextAvailableSlot() est appelée avec le bon type de véhicule
    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);

    assertNull(parkingSpot);
}

@Test
public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    // Mock méthode readSelection() pour renvoyer 3
    when(inputReaderUtil.readSelection()).thenReturn(3);

    // Appel de la méthode à tester
    ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

    // Vérifie que readSelection() est appelée
    verify(inputReaderUtil, times(1)).readSelection();

    // Vérifiez que getNextAvailableSlot() n'est pas appelée
    verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));

    // Vérifiez que le résultat est "null" 
    assertNull(result);
//  verify(result==null);

}
}