package app.mystro.processor.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.NativeLordOfOrb;
import app.common.model.NativeLordOfOrbEntry;
import app.common.model.NativePlanetaryHour;
import app.mystro.processor.MystroProcessor;

public final class LordOfOrbProcessor extends MystroProcessor {
    private static final int YEARS_TO_EMIT = 84;

    @Override
    public void populate(NativeReportBuilder builder) {
        NativePlanetaryHour planetaryHour = builder.planetaryHour();
        if (planetaryHour == null || planetaryHour.hourRuler() == null) {
            throw new IllegalArgumentException("Planetary hour must be populated before Lord of the Orb");
        }

        String startingRuler = planetaryHour.hourRuler();
        int baseIndex = Config.CHALDEAN_YEAR_SEQUENCE.indexOf(startingRuler);
        if (baseIndex < 0) {
            throw new IllegalArgumentException("Unsupported starting ruler for Lord of the Orb: " + startingRuler);
        }

        OffsetDateTime birthDateTime = builder.nativeChart().birthDateTime();
        List<NativeLordOfOrbEntry> years = new ArrayList<>();
        for (int age = 0; age < YEARS_TO_EMIT; age++) {
            years.add(new NativeLordOfOrbEntry(
                    age,
                    birthDateTime.plusYears(age).toLocalDate().toString(),
                    rulerAt(baseIndex, age),
                    rulerAt(baseIndex, age % 12)));
        }

        NativeLordOfOrb lordOfOrb = new NativeLordOfOrb();
        lordOfOrb.setStartingRuler(startingRuler);
        lordOfOrb.setYears(years);
        builder.lordOfOrb(lordOfOrb);
    }

    private String rulerAt(int baseIndex, int offset) {
        return Config.CHALDEAN_YEAR_SEQUENCE.get(Math.floorMod(baseIndex + offset, Config.CHALDEAN_YEAR_SEQUENCE.size()));
    }
}
