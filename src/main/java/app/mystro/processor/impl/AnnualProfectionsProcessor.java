package app.mystro.processor.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.common.model.NativeAnnualProfectionEntry;
import app.common.model.NativeAnnualProfections;
import app.common.model.NativeHermeticLot;
import app.common.model.NativeLordOfOrb;
import app.common.model.NativeLordOfOrbEntry;
import app.common.model.NativeChart;
import app.mystro.processor.MystroProcessor;

public final class AnnualProfectionsProcessor extends MystroProcessor {
    private static final int YEARS_TO_EMIT = 84;

    @Override
    public void populate(NativeReportBuilder builder) {
        NativeChart chart = builder.nativeChart();
        NativeLordOfOrb lordOfOrb = builder.lordOfOrb();
        NativeHermeticLot fortune = builder.lot("Fortune");
        if (chart == null || lordOfOrb == null || fortune == null) {
            throw new IllegalArgumentException("Chart, Lord of the Orb, and Fortune must be populated before Annual Profections");
        }

        OffsetDateTime birthDateTime = chart.birthDateTime();
        ChartPoint mc = chart.point("MC");
        ChartPoint sun = chart.point("Sun");
        ChartPoint moon = chart.point("Moon");
        List<NativeAnnualProfectionEntry> years = new ArrayList<>();
        for (NativeLordOfOrbEntry orbYear : lordOfOrb.years()) {
            int age = orbYear.age();
            String lordOfYearSign = advanceSign(chart.ascSign(), age);
            years.add(new NativeAnnualProfectionEntry(
                    age,
                    birthDateTime.plusYears(age).toLocalDate().toString(),
                    lordOfYearSign,
                    Config.SIGN_RULERS.get(lordOfYearSign),
                    orbYear.mod84(),
                    orbYear.mod12(),
                    advanceSign(mc.sign(), age),
                    advanceSign(sun.sign(), age),
                    advanceSign(moon.sign(), age),
                    advanceSign(fortune.sign(), age)));
            if (years.size() >= YEARS_TO_EMIT) {
                break;
            }
        }

        builder.annualProfections(new NativeAnnualProfections(years));
    }

    private String advanceSign(String startSign, int years) {
        int startIndex = Config.SIGNS.indexOf(startSign);
        if (startIndex < 0) {
            throw new IllegalArgumentException("Unsupported sign for annual profections: " + startSign);
        }
        return Config.SIGNS.get(Math.floorMod(startIndex + years, Config.SIGNS.size()));
    }
}
