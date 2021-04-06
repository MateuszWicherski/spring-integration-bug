package pl.wicherski.spring.springintegrationbug;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;

import java.io.File;

import static java.time.Duration.ofSeconds;
import static org.springframework.integration.dsl.Pollers.fixedRate;
import static org.springframework.integration.handler.LoggingHandler.Level.INFO;

@Configuration
public class IntegrationConfig {

    private static final String FILE_READ_CHANNEL = "file-read";

    @Bean
    public IntegrationFlow fileReadingFlow() {
        return IntegrationFlows.from(
                Files.inboundAdapter(new File("source")),
                config -> config.poller(fixedRate(ofSeconds(10))))
                .filter(new SimplePatternFileListFilter("*.txt")) // FIXME - registering this filter is throwing an exception from spring-boot 2.4.3 (works in 2.4.2)
                .channel(FILE_READ_CHANNEL)
                .get();
    }

    @Bean
    public IntegrationFlow fileLoggingFlow() {
        return IntegrationFlows.from(FILE_READ_CHANNEL)
                .log(INFO, message -> "Processing file " + message.getHeaders().get(FileHeaders.FILENAME))
                .get();
    }
}
