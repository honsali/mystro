package app.common.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonFileSupport {
    private static final class TwoDecimalDoubleSerializer extends JsonSerializer<Double> {
        @Override
        public void serialize(Double value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            if (value == null) {
                generator.writeNull();
                return;
            }
            BigDecimal rounded = BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).stripTrailingZeros();
            generator.writeNumber(rounded);
        }
    }

    private static final SimpleModule DOUBLE_ROUNDING_MODULE = new SimpleModule().addSerializer(Double.class, new TwoDecimalDoubleSerializer()).addSerializer(Double.TYPE, new TwoDecimalDoubleSerializer());

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(DOUBLE_ROUNDING_MODULE).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).enable(SerializationFeature.INDENT_OUTPUT);

    public static void write(Path path, Object value) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        MAPPER.writeValue(path.toFile(), value);
    }

    public static <T> T read(Path path, Class<T> type) throws IOException {
        return MAPPER.readValue(path.toFile(), type);
    }

    private JsonFileSupport() {}
}
