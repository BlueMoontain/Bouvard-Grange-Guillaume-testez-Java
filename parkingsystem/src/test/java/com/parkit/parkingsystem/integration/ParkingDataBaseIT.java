package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){
    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        // check that a ticket is actually saved in DB
        String vehicleRegNumber = "ABCDEF"; 
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
    
        assertNotNull(ticket);
        assertEquals(vehicleRegNumber, ticket.getVehicleRegNumber());

        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        
        //  check if parking table is updated with availability
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertNotNull(parkingSpot);
        assertFalse(parkingSpot.isAvailable());
        assertEquals(parkingSpot.getId(), ticket.getParkingSpot().getId());
    }

    @Test
    public void testParkingLotExit(){       
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        try{
            TimeUnit.SECONDS.sleep(2);
        } catch(InterruptedException e) {
		    e.printStackTrace();
		}
        parkingService.processExitingVehicle();

        // check that the fare generated and out time are populated correctly in the database
        String vehicleRegNumber = "ABCDEF"; 
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        assertNotNull(ticket);

        String response = "ExitCheck; result = "+ticket.getOutTime();

        assertNotNull(response, ticket.getOutTime());
        assertNotNull(ticket.getPrice());   
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        testParkingLotExit();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = new Ticket();

        ticket.setInTime(new Date(System.currentTimeMillis() - (  120 * 60 * 1000))); //testing 2 hours duration for ex. 
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();

        // Verify if 5% discount is applied for recurring user
        ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);

        Double expectedPrice = (double)Math.round(Fare.CAR_RATE_PER_HOUR * 2 * 0.95*100)/100;

        assertTrue(expectedPrice == ticket.getPrice());
        }

}