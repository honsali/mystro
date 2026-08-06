package app.reading.description.valens.calculator;

import java.util.Comparator;
import java.util.List;

import app.chart.TraditionalTables;
import app.chart.model.HousePosition;
import app.chart.model.Chart;
import app.reading.description.common.model.HouseTopicRulerEntry;

public final class ValensHouseTopicRulerCalculator {

    public List<HouseTopicRulerEntry> calculate(Chart chart) {
        if (chart.getHouses() == null) {
            return List.of();
        }
        return chart.getHouses().stream()
                .sorted(Comparator.comparingInt(HousePosition::getHouse))
                .map(house -> new HouseTopicRulerEntry(
                        house.getHouse(),
                        house.getSign(),
                        TraditionalTables.domicileRuler(house.getSign())
                ))
                .toList();
    }
}
