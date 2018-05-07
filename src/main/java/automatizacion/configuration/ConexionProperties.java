package automatizacion.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Conexion Properties
 * 
 * @author David Martos Grande
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "conexion")
public class ConexionProperties {
    
    @Getter
    @Setter
    private String apiMultiUploadDev;
    @Getter
    @Setter
    private String apiMultiUploadTest;
    @Getter
    @Setter
    private String apiMultiUploadPro;

}
