package com.parkit.parkingsystem.util;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundUtil {

    // Méthode statique pour arrondir à 2 décimales
    public static double roundToTwoDecimals(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();

    }
}
