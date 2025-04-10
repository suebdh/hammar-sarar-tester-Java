package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        //Passage de millisecondes à heures
        float durationInHours = (outTime - inTime) / 3600000f;

        // Si la durée est inférieure à 30 minutes, le prix doit être égal à 0
        if (durationInHours < 0.5) {
            ticket.setPrice(0);
        } else {
            double fare;
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    fare = durationInHours * Fare.CAR_RATE_PER_HOUR;
                    break;
                }
                case BIKE: {
                    fare = durationInHours * Fare.BIKE_RATE_PER_HOUR;
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }

            if (discount) {
                ticket.setPrice(fare * Fare.RATE_AFTER_REDUCTION);
            } else {
                ticket.setPrice(fare);
            }
        }

    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}