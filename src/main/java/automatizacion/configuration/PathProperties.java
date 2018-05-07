package automatizacion.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * PathProperties
 *
 * @author David Martos Grande
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "path")
public class PathProperties {

    @Setter
    private String pendientes;
    
    @Setter
    private String finalizadas;

    public Path getPendientes() {
        return Paths.get(pendientes);
    }

    public Path getFinalizadas() {
        return Paths.get(finalizadas);
    }

}
