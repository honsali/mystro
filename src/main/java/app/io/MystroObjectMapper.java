package app.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Shared ObjectMapper factory for Mystro command-line JSON output.
 */
public final class MystroObjectMapper {

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
