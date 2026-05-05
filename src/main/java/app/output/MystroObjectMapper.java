package app.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Shared ObjectMapper factory for Mystro JSON conventions.
 * Both CLI file output and REST response serialization should use this.
 */
public final class MystroObjectMapper {

    /**
     * Creates a new ObjectMapper configured with Mystro conventions:
     * - RoundedDoubleSerializer for six-decimal rounding (boxed and primitive)
     * - JavaTimeModule and Jdk8Module
     * - Dates serialized as strings, not timestamps
     */
    public static ObjectMapper create() {
        SimpleModule roundingModule = new SimpleModule()
                .addSerializer(Double.class, new RoundedDoubleSerializer())
                .addSerializer(Double.TYPE, new RoundedDoubleSerializer());
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .registerModule(roundingModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private MystroObjectMapper() {}
}
