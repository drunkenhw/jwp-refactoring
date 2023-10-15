package kitchenpos.fixture;

import kitchenpos.application.dto.OrderRequest;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;

public class OrderLineItemFixture {
    public static OrderLineItem orderLineItem(Order order, Long menuId, long quantity) {
        return new OrderLineItem(order, menuId, quantity);
    }

    public static OrderRequest.OrderLineItemRequest orderLineItemRequest(Long menuId, long quantity) {
        return new OrderRequest.OrderLineItemRequest(menuId, quantity);
    }
}
