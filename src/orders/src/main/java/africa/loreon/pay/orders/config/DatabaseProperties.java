package africa.loreon.pay.orders.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = DatabaseProperties.PREFIX)
@Getter
@Setter
public class DatabaseProperties {

  public static final String PREFIX = "loreonpay.orders.persistence";

  @NotEmpty
  private String endpoint;

  @NotEmpty
  private String name;

  @NotEmpty
  private String username;

  @NotEmpty
  private String password;
}
