package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();
        long duration = outHour - inHour;
      
        if (duration < (30 * 60 * 1000)) { // <30min = free parking
            ticket.setPrice(0); 
        } 

        else {
        double ratePerHour = 0.0;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR:
                ratePerHour = duration * Fare.CAR_RATE_PER_HOUR / (1000.0 * 60.0 * 60.0);
                break;
            
            case BIKE:
                ratePerHour = duration * Fare.BIKE_RATE_PER_HOUR / (1000.0 * 60.0 * 60.0);
                break;
            
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        if (discount) {
            ratePerHour *= 0.95; // 5% discount
        }
        ticket.setPrice(ratePerHour);
        }  
    }
        public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false); // no discount
    }
}
