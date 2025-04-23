package com.parkit.parkingsystem;

import com.parkit.parkingsystem.util.RoundUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

//Classe de test pour la classe utilitaire RoundUtil avec RoundingMode.HALF_DOWN
// Cette classe vérifie que la méthode roundToTwoDecimals fonctionne correctement
// avec différents cas d'arrondi tels que les valeurs proches de 0.5, les valeurs négatives,
// les valeurs exactes et les cas limites (comme 0.005 et 0.015).
public class RoundUtilTest {

    // Deux Tests classique d'arrondi avec des valeurs qui doivent être arrondies à l'inférieur
    @Test
    public void roundToTwoDecimals_shouldRoundExactHalfDownCorrectly() {
        double value = 2.555;
        double expected = 2.56;
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 2.55 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    @Test
    public void roundToTwoDecimals_shouldRoundStrictlyBelowHalfCorrectly() {
        double value = 2.554;
        double expected = 2.55;
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 2.55 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Test d'arrondi avec des valeurs qui doivent être arrondies à l'extérieur
    @Test
    public void roundToTwoDecimals_shouldRoundStrictlyAboveHalfCorrectly() {

        // Avec HALF_DOWN, les valeurs strictement au-dessus de la moitié sont arrondies vers le haut
        double value = 2.556;  // Exemple avec un nombre qui devrait être arrondi vers le haut
        double expected = 2.56; // Cela devrait être arrondi à 2.56
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 2.56 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Cas avec zéro
    @Test
    public void roundToTwoDecimals_shouldHandleZeroCorrectly() {
        double value = 0.0;
        double expected = 0.0; // l'arrondi de zéro reste zéro
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 0.0 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Cas avec des valeurs négatives
    @Test
    public void roundToTwoDecimals_shouldHandleNegativeValues() {
        double value = -1.234;
        double expected = -1.23;
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected -1.23 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Deux Cas limites : valeurs proches des seuils d'arrondi
    @Test
    public void roundToTwoDecimals_shouldRoundEdgeCase005Correctly() {
        double value = 0.005;
        double expected = 0.01;
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 0.00 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    @Test
    public void roundToTwoDecimals_shouldRoundEdgeCase015Correctly() {
        double value = 0.015;
        double expected = 0.01;
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 0.01 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Cas où le nombre est exactement à 2 décimales, donc pas besoin d'arrondi
    @Test
    public void roundToTwoDecimals_shouldHandleExactTwoDecimals() {
        double value = 2.45; // Un nombre déjà à deux décimales
        double expected = 2.45; // Aucun arrondi nécessaire
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 2.45 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Cas où le nombre est un entier, donc pas besoin d'arrondi
    @Test
    public void roundToTwoDecimals_shouldHandleIntegerCorrectly() {
        double value = 5.0;
        double expected = 5.00;
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 5.00 but got " + RoundUtil.roundToTwoDecimals(value));
    }

    // Cas avec une petite valeur décimale
    @Test
    public void roundToTwoDecimals_shouldHandleSmallDecimal() {
        double value = 0.999; // Un nombre très proche de 1
        double expected = 1.00; // Devrait être arrondi à 1.00
        assertEquals(expected, RoundUtil.roundToTwoDecimals(value),
                "Expected 1.00 but got " + RoundUtil.roundToTwoDecimals(value));
    }
}
