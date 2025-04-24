package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.RoundUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        //Passage de millisecondes à heures : 3600000f équivalent à 60.0*60.0*1000.0
        double durationInHours = (outTime - inTime) / 3_600_000.0;
        durationInHours = BigDecimal.valueOf(durationInHours).setScale(2, RoundingMode.HALF_UP).doubleValue();

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
                ticket.setPrice(RoundUtil.roundToTwoDecimals(fare * Fare.RATE_AFTER_REDUCTION));
            } else {
                ticket.setPrice(RoundUtil.roundToTwoDecimals(fare));
            }

        }

    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}