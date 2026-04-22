package app.mystro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import app.common.Config;
import app.common.Logger;
import app.common.NativeReportBuilder;
import app.common.io.JsonFileSupport;
import app.common.io.NativeListLoader;
import app.common.model.NativeBirth;
import app.common.model.NativeReport;
import app.mystro.processor.MystroProcessor;
import app.mystro.processor.impl.AspectsProcessor;
import app.mystro.processor.impl.ChartProcessor;
import app.mystro.processor.impl.DerivedChartsProcessor;
import app.mystro.processor.impl.HermeticLotsProcessor;
import app.mystro.processor.impl.HousesProcessor;
import app.mystro.processor.impl.LordOfOrbProcessor;
import app.mystro.processor.impl.PlanetPositionsProcessor;
import app.mystro.processor.impl.PlanetaryHourProcessor;
import app.mystro.processor.impl.SyzygyProcessor;
import app.swisseph.core.SwissEph;

public final class MystroService {

    private final List<MystroProcessor> sectionProcessors = List.of(//
            new ChartProcessor(), //
            new PlanetaryHourProcessor(), //
            new LordOfOrbProcessor(), //
            new SyzygyProcessor(), //
            new HermeticLotsProcessor(), //
            new PlanetPositionsProcessor(), //
            new HousesProcessor(), //
            new AspectsProcessor(), //
            new DerivedChartsProcessor()//
    );

    public List<String> generate(List<String> names) throws IOException {
        List<String> written = new ArrayList<>();
        Map<String, NativeBirth> birthDataByName = NativeListLoader.loadByName(Config.NATIVE_LIST_PATH);
        for (String name : names) {
            NativeBirth birthData = birthDataByName.get(name);
            if (birthData == null) {
                Logger.getInstance().missingNativeConfig(name);
                continue;
            }
            NativeReport report = buildNativeReport(name, birthData);
            JsonFileSupport.write(Config.MYSTRO_OUTPUT_DIR.resolve(name + ".json"), report);
            written.add(name);
        }
        return written;
    }

    public NativeReport buildNativeReport(String name, NativeBirth birth) throws IOException {
        SwissEph swissEph = new SwissEph(Config.EPHE_DIR.toAbsolutePath().normalize().toString());
        NativeReportBuilder builder = new NativeReportBuilder(name, birth, swissEph);
        for (MystroProcessor processor : sectionProcessors) {
            processor.populate(builder);
        }
        return builder.build();
    }
}
