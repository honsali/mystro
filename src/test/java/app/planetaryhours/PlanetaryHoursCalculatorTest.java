package app.planetaryhours;

import app.chart.data.Planet;
import app.testing.SyntheticTestData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanetaryHoursCalculatorTest {

    private final PlanetaryHoursCalculator calculator = new PlanetaryHoursCalculator();

    @Test
    void calculatesFullPlanetaryDayForZoomOutput() {
        PlanetaryHoursInput input = new PlanetaryHoursInput(
                SyntheticTestData.SUBJECT_ID,
                SyntheticTestData.BIRTH_DATE,
                SyntheticTestData.BIRTH_DATE_TIME.getOffset(),
                SyntheticTestData.LATITUDE,
                SyntheticTestData.LONGITUDE);

        PlanetaryHoursCalculation calculation = calculator.calculateFullPlanetaryDay(input);

        assertEquals(Planet.SATURN, calculation.getDayRuler());
        assertEquals(SyntheticTestData.BIRTH_DATE, calculation.getCoverageStart().getDate());
        assertEquals("08:05", calculation.getCoverageStart().getTime());
        assertEquals(SyntheticTestData.BIRTH_DATE.plusDays(1), calculation.getCoverageEnd().getDate());
        assertEquals("08:05", calculation.getCoverageEnd().getTime());
        assertEquals(24, calculation.getHours().size());

        PlanetaryHourEntry first = calculation.getHours().get(0);
        assertEquals(1, first.getSequence());
        assertEquals(1, first.getHour());
        assertEquals(PlanetaryHourPeriod.DAY, first.getPeriod());
        assertEquals(Planet.SATURN, first.getRuler());
        assertEquals("08:05", first.getStartTime());
        assertEquals("08:45", first.getEndTime());

        PlanetaryHourEntry last = calculation.getHours().get(23);
        assertEquals(24, last.getSequence());
        assertEquals(24, last.getHour());
        assertEquals(PlanetaryHourPeriod.NIGHT, last.getPeriod());
        assertEquals(Planet.MARS, last.getRuler());
        assertEquals(SyntheticTestData.BIRTH_DATE.plusDays(1), last.getEndDate());
        assertEquals("08:05", last.getEndTime());
    }

    @Test
    void calculatesPlanetaryHoursForTheCivilBirthDateWithMidpointTables() {
        PlanetaryHoursInput input = new PlanetaryHoursInput(
                SyntheticTestData.SUBJECT_ID,
                SyntheticTestData.BIRTH_DATE,
                SyntheticTestData.BIRTH_DATE_TIME.getOffset(),
                SyntheticTestData.LATITUDE,
                SyntheticTestData.LONGITUDE);

        PlanetaryHoursCalculation calculation = calculator.calculate(input);

        assertEquals(Planet.SATURN, calculation.getDayRuler());
        assertEquals(SyntheticTestData.BIRTH_DATE, calculation.getCoverageStart().getDate());
        assertEquals("00:00", calculation.getCoverageStart().getTime());
        assertEquals(SyntheticTestData.BIRTH_DATE.plusDays(1), calculation.getCoverageEnd().getDate());
        assertEquals("00:00", calculation.getCoverageEnd().getTime());
        assertEquals("08:05", calculation.getSunrise().getTime());
        assertEquals("16:01", calculation.getSunset().getTime());
        assertEquals("08:05", calculation.getNextSunrise().getTime());
        assertEquals(25, calculation.getHours().size());

        PlanetaryHourEntry first = calculation.getHours().get(0);
        assertEquals(1, first.getSequence());
        assertEquals(18, first.getHour());
        assertEquals(SyntheticTestData.BIRTH_DATE.minusDays(1), first.getPlanetaryDayDate());
        assertEquals(PlanetaryHourPeriod.NIGHT, first.getPeriod());
        assertEquals(Planet.SATURN, first.getRuler());
        assertEquals("\u2644", first.getRulerGlyph());
        assertEquals(SyntheticTestData.BIRTH_DATE, first.getStartDate());
        assertEquals("00:00", first.getStartTime());
        assertEquals("00:02", first.getEndTime());
        assertEquals("22:42", first.getFullPlanetaryHourStart().getTime());
        assertEquals("00:02", first.getFullPlanetaryHourEnd().getTime());
        assertEquals("00:01", first.getMidpoint().getTime());
        assertEquals("LIBRA", first.getMidpointChart().getAscendantSign().name());

        PlanetaryHourEntry firstCurrentDayHour = calculation.getHours().get(7);
        assertEquals(8, firstCurrentDayHour.getSequence());
        assertEquals(1, firstCurrentDayHour.getHour());
        assertEquals(SyntheticTestData.BIRTH_DATE, firstCurrentDayHour.getPlanetaryDayDate());
        assertEquals(PlanetaryHourPeriod.DAY, firstCurrentDayHour.getPeriod());
        assertEquals(Planet.SATURN, firstCurrentDayHour.getRuler());
        assertEquals("\u2644", firstCurrentDayHour.getRulerGlyph());
        assertEquals("08:05", firstCurrentDayHour.getStartTime());
        assertEquals("08:45", firstCurrentDayHour.getEndTime());
        assertEquals("08:25", firstCurrentDayHour.getMidpoint().getTime());
        assertEquals("WHOLE_SIGN", firstCurrentDayHour.getMidpointChart().getHouseSystem().name());
        assertEquals("CAPRICORN", firstCurrentDayHour.getMidpointChart().getAscendantSign().name());
        assertEquals(12, firstCurrentDayHour.getMidpointChart().getHouseSignPlanets().size());
        assertEquals("CAPRICORN", firstCurrentDayHour.getMidpointChart().getHouseSignPlanets().get(0).getSign().name());
        assertEquals(Planet.SUN, firstCurrentDayHour.getMidpointChart().getHouseSignPlanets().get(0).getPlanets().get(0).getPlanet());

        PlanetaryHourEntry firstCurrentNightHour = calculation.getHours().get(19);
        assertEquals(20, firstCurrentNightHour.getSequence());
        assertEquals(13, firstCurrentNightHour.getHour());
        assertEquals(PlanetaryHourPeriod.NIGHT, firstCurrentNightHour.getPeriod());
        assertEquals(Planet.MERCURY, firstCurrentNightHour.getRuler());
        assertEquals("16:01", firstCurrentNightHour.getStartTime());
        assertEquals("17:21", firstCurrentNightHour.getEndTime());
        assertEquals("16:41", firstCurrentNightHour.getMidpoint().getTime());
        assertEquals("CANCER", firstCurrentNightHour.getMidpointChart().getAscendantSign().name());

        PlanetaryHourEntry last = calculation.getHours().get(24);
        assertEquals(25, last.getSequence());
        assertEquals(18, last.getHour());
        assertEquals(Planet.SUN, last.getRuler());
        assertEquals(SyntheticTestData.BIRTH_DATE.plusDays(1), last.getEndDate());
        assertEquals("00:00", last.getEndTime());
        assertEquals("00:03", last.getFullPlanetaryHourEnd().getTime());
        assertEquals("23:21", last.getMidpoint().getTime());
    }

    @Test
    void appliesObserverElevationToSunriseCalculations() {
        PlanetaryHoursInput seaLevel = new PlanetaryHoursInput(
                SyntheticTestData.SUBJECT_ID,
                SyntheticTestData.BIRTH_DATE,
                SyntheticTestData.BIRTH_DATE_TIME.getOffset(),
                SyntheticTestData.LATITUDE,
                SyntheticTestData.LONGITUDE,
                0.0);
        PlanetaryHoursInput highElevation = new PlanetaryHoursInput(
                SyntheticTestData.SUBJECT_ID,
                SyntheticTestData.BIRTH_DATE,
                SyntheticTestData.BIRTH_DATE_TIME.getOffset(),
                SyntheticTestData.LATITUDE,
                SyntheticTestData.LONGITUDE,
                2_000.0);

        PlanetaryHoursCalculation seaLevelCalculation = calculator.calculateFullPlanetaryDay(seaLevel);
        PlanetaryHoursCalculation highElevationCalculation = calculator.calculateFullPlanetaryDay(highElevation);

        assertEquals("08:05", seaLevelCalculation.getSunrise().getTime());
        assertEquals("08:06", highElevationCalculation.getSunrise().getTime());
    }
}
