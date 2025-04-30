package com.parkit.parkingsystem.util;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundUtil {

    // Méthode statique pour arrondir à 2 décimales
    public static double roundToTwoDecimals(double value) {
        return new BigDecimal(String.valueOf(value)) // String better than BigDecimal for precision
                .setScale(2, RoundingMode.HALF_DOWN)
                .doubleValue();
        //HALF_DOWN : Eviter la surtaxation et favoriser l'expérience utilisateur
    }
}
