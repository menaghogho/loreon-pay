package africa.loreon.pay.orders.web.payload;

import africa.loreon.pay.orders.entities.ShippingAddressEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {
  ShippingAddress toShippingAddress(ShippingAddressEntity entity);

  ShippingAddressEntity toShippingAddressEntity(ShippingAddress item);
}
