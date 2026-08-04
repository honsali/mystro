package app.reading.description.valens.calculator;

import java.util.List;
import java.util.Map;

import app.chart.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.reading.description.common.model.DerivedHouseFramesEntry;
import app.reading.description.common.model.DerivedHouseFramesEntry.DerivedHouseFrameEntry;
import app.reading.description.common.model.DerivedHouseFramesEntry.DerivedHousePlaceEntry;
import app.reading.description.common.model.LotEntry;

public final class ValensDerivedHouseFrameCalculator {

    public DerivedHouseFramesEntry calculate(NatalChart chart) {
        return new DerivedHouseFramesEntry(frame(chart, "FORTUNE"), frame(chart, "SPIRIT"));
    }

    private DerivedHouseFrameEntry frame(NatalChart chart, String lotName) {
        LotEntry lot = lot(chart, lotName);
        if (lot == null) {
            return null;
        }
        return new DerivedHouseFrameEntry(
                lot.name(),
                lot.displayName(),
                lot.doctrine(),
                lot.sign(),
                lot.house(),
                places(chart, lot.sign())
        );
    }

    private List<DerivedHousePlaceEntry> places(NatalChart chart, ZodiacSign anchorSign) {
        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(houseFromLot -> {
                    ZodiacSign sign = signFrom(anchorSign, houseFromLot);
                    return new DerivedHousePlaceEntry(
                            houseFromLot,
                            sign,
                            natalHouse(chart, sign),
                            TraditionalTables.domicileRuler(sign),
                            occupiedPoints(chart, sign)
                    );
                })
                .toList();
    }

    private LotEntry lot(NatalChart chart, String name) {
        if (chart.getLots() == null) {
            return null;
        }
        return chart.getLots().stream()
                .filter(candidate -> name.equals(candidate.name()))
                .findFirst()
                .orElse(null);
    }

    private ZodiacSign signFrom(ZodiacSign anchorSign, int houseFromLot) {
        ZodiacSign[] signs = ZodiacSign.values();
        return signs[Math.floorMod(anchorSign.ordinal() + houseFromLot - 1, signs.length)];
    }

    private int natalHouse(NatalChart chart, ZodiacSign sign) {
        ZodiacSign ascendantSign = chart.requireAngle(AngleType.ASCENDANT).getSign();
        return Math.floorMod(sign.ordinal() - ascendantSign.ordinal(), ZodiacSign.values().length) + 1;
    }

    private List<PointKey> occupiedPoints(NatalChart chart, ZodiacSign sign) {
        if (chart.getPoints() == null) {
            return List.of();
        }
        return chart.getPoints().entrySet().stream()
                .filter(entry -> signOf(entry.getValue()) == sign)
                .map(Map.Entry::getKey)
                .toList();
    }

    private ZodiacSign signOf(PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint.sign();
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return anglePoint.sign();
        }
        throw new IllegalArgumentException("Unsupported point entry " + point.getClass().getName());
    }
}
