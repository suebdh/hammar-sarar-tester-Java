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

        //Moving from milliseconds to hours : 3_600_000.0 équivalent à 60.0*60.0*1000.0
        double durationInHours = (outTime - inTime) / 3_600_000.0;
        durationInHours = BigDecimal.valueOf(durationInHours).setScale(2, RoundingMode.HALF_UP).doubleValue();

        // If the duration is less than 30 minutes, the price must be equal to 0
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
                    throw new IllegalArgumentException("Unknown Parking Type");
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