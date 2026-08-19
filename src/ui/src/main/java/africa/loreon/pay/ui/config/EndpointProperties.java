package africa.loreon.pay.ui.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("loreonpay.ui.endpoints")
@Data
public class EndpointProperties {

  private String catalog;

  private String carts;

  private String checkout;

  private String orders;
}
