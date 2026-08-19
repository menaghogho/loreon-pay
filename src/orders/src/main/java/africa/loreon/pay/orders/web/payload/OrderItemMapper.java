package africa.loreon.pay.orders.web.payload;

import africa.loreon.pay.orders.entities.OrderItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
  OrderItem toOrderItem(OrderItemEntity entity);

  OrderItemEntity toOrderItemEntity(OrderItem item);
}
