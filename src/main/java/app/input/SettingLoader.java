package app.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import app.chart.data.CalculationPrecision;
import app.input.model.CalculationSetting;
import app.input.model.InputListBundle;
import app.output.Logger;

public final class SettingLoader {
    public void load(InputListBundle input) throws IOException {
        Properties properties = new Properties();
        Path path = Path.of("input", "settings.properties");
        if (Files.exists(path)) {
            try (InputStream stream = Files.newInputStream(path)) {
                properties.load(stream);
            }
        }

        CalculationPrecision precision = parsePrecision(properties);
        input.setCalculationSetting(new CalculationSetting(precision));
    }

    private CalculationPrecision parsePrecision(Properties properties) {
        String value = properties.getProperty("calculation.precision", CalculationPrecision.STANDARD.name());
        try {
            return CalculationPrecision.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.instance.error("settings", "Unknown calculation.precision: " + value);
            return CalculationPrecision.STANDARD;
        }
    }
}
