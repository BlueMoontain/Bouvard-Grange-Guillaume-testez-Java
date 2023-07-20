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
        // if (duration < (30*60*1000)) { //less than 30 minutes (in milliseconds) = free parking
        //     ticket.setPrice(0); 
        // }

        // else {
        //     switch (ticket.getParkingSpot().getParkingType()){
        //         case CAR: {
        //             double ratePerHour = duration * Fare.CAR_RATE_PER_HOUR/ (1000 * 60 * 60);
        //             if (discount) {
        //                     ratePerHour *= 0.95; // 5% discount
        //                 }
        //                 ticket.setPrice(ratePerHour);
        //         break;
        //     }
        //         case BIKE: {
        //             double ratePerHour = duration * Fare.BIKE_RATE_PER_HOUR/ (1000 * 60 * 60);
        //             if (discount) {
        //                      ratePerHour *= 0.95; // 
        //                 }
        //                 ticket.setPrice(ratePerHour);
        //         break;
        //     }
        //     default: throw new IllegalArgumentException("Unkown Parking Type");
        // }
        // // revoir discount pour opti méthode (lignes qui se repetent, peut etre que nom n'est pas le plus clair mais attention a tous les changer si modif)
        // }
    }
    
        public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false); // no discount
    }
}