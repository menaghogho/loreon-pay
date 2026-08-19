package africa.loreon.pay.orders.config.messaging;

import java.util.Properties;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertiesPropertySource;

public class ApplicationEnvironmentPreparedListener
  implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    var environment = event.getEnvironment();

    var messagingProvider = environment.getProperty(
      "loreonpay.orders.messaging.provider"
    );

    if (messagingProvider.equals("rabbitmq")) {
      Properties props = new Properties();
      props.put(
        "spring.rabbitmq.addresses",
        environment.getProperty("loreonpay.orders.messaging.rabbitmq.addresses")
      );
      props.put(
        "spring.rabbitmq.username",
        environment.getProperty("loreonpay.orders.messaging.rabbitmq.username")
      );
      props.put(
        "spring.rabbitmq.password",
        environment.getProperty("loreonpay.orders.messaging.rabbitmq.password")
      );
      environment
        .getPropertySources()
        .addFirst(new PropertiesPropertySource("rabbitmqProps", props));
    }
  }
}
