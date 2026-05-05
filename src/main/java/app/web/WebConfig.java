package app.web;

import app.basic.BasicCalculator;
import app.input.DoctrineLoader;
import app.output.JsonReportWriter;
import app.output.MystroObjectMapper;
import app.runtime.DescriptiveReportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${mystro.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = parseOrigins(allowedOrigins);
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type");
    }

    /**
     * Parse comma-separated origins: trim whitespace, ignore blanks.
     * If the result is empty, return the default local dev origins.
     */
    static String[] parseOrigins(String raw) {
        String[] parsed = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (parsed.length == 0) {
            return new String[]{"http://localhost:5173", "http://localhost:3000"};
        }
        return parsed;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return MystroObjectMapper.create();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public BasicCalculator basicCalculator() {
        return new BasicCalculator();
    }

    @Bean
    public JsonReportWriter jsonReportWriter() {
        return new JsonReportWriter();
    }

    @Bean
    public DoctrineLoader doctrineLoader() {
        return new DoctrineLoader();
    }

    @Bean
    public DescriptiveReportService descriptiveReportService(
            BasicCalculator basicCalculator, JsonReportWriter jsonReportWriter) {
        return new DescriptiveReportService(basicCalculator, jsonReportWriter);
    }
}
