package app.output;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public final class RoundedDoubleSerializer extends JsonSerializer<Double> {
    private static final double DECIMAL_6_FACTOR = 1_000_000.0;

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (!Double.isFinite(value)) {
            gen.writeNumber(value);
            return;
        }
        gen.writeNumber(Math.round(value * DECIMAL_6_FACTOR) / DECIMAL_6_FACTOR);
    }
}
