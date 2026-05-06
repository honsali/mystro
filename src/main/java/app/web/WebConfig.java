package app.web;

import app.basic.BasicCalculator;
import app.input.DoctrineLoader;
import app.output.MystroObjectMapper;
import app.runtime.DescriptiveReportGenerator;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.normalizedAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type");
    }

    @Bean
    public MappingJackson2HttpMessageConverter jacksonConverter() {
        return new MappingJackson2HttpMessageConverter(MystroObjectMapper.create());
    }

    @Bean
    public BasicCalculator basicCalculator() {
        return new BasicCalculator();
    }

    @Bean
    public DoctrineLoader doctrineLoader() {
        return new DoctrineLoader();
    }

    @Bean
    public DescriptiveReportGenerator descriptiveReportGenerator(BasicCalculator basicCalculator) {
        return new DescriptiveReportGenerator(basicCalculator);
    }
}
