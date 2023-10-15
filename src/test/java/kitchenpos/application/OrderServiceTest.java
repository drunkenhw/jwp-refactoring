package kitchenpos.application;

import kitchenpos.application.dto.OrderChangeStatusRequest;
import kitchenpos.application.dto.OrderRequest;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuRepository;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderLineItemRepository;
import kitchenpos.domain.OrderRepository;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.OrderTableRepository;
import kitchenpos.fake.InMemoryMenuRepository;
import kitchenpos.fake.InMemoryOrderLineItemRepository;
import kitchenpos.fake.InMemoryOrderRepository;
import kitchenpos.fake.InMemoryOrderTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.Long.MAX_VALUE;
import static kitchenpos.application.dto.OrderRequest.OrderLineItemRequest;
import static kitchenpos.domain.OrderStatus.COOKING;
import static kitchenpos.domain.OrderStatus.MEAL;
import static kitchenpos.fixture.MenuFixture.menu;
import static kitchenpos.fixture.OrderFixture.order;
import static kitchenpos.fixture.OrderFixture.orderRequest;
import static kitchenpos.fixture.OrderLineItemFixture.orderLineItem;
import static kitchenpos.fixture.OrderLineItemFixture.orderLineItemRequest;
import static kitchenpos.fixture.OrderTableFixtrue.orderTable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderServiceTest {

    private MenuRepository menuRepository;
    private OrderRepository orderRepository;
    private OrderLineItemRepository orderLineItemRepository;
    private OrderTableRepository orderTableRepository;
    private OrderService orderService;

    @BeforeEach
    void before() {
        menuRepository = new InMemoryMenuRepository();
        orderRepository = new InMemoryOrderRepository();
        orderLineItemRepository = new InMemoryOrderLineItemRepository();
        orderTableRepository = new InMemoryOrderTableRepository();
        orderService = new OrderService(menuRepository, orderRepository, orderLineItemRepository, orderTableRepository);
    }

    @Test
    void 주문_항목이_없다면_예외가_발생한다() {
        // given
        OrderTable savedOrderTable = orderTableRepository.save(orderTable(10, false));
        OrderRequest orderRequest = orderRequest(savedOrderTable.getId(), List.of());

        // expect
        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목은 하나 이상이여야 합니다");
    }

    @Test
    void 주문_항목의_메뉴가_존재하지_않는다면_예외가_발생한다() {
        // given
        OrderTable savedOrderTable = orderTableRepository.save(orderTable(10, false));
        OrderRequest request = orderRequest(savedOrderTable.getId(), List.of(orderLineItemRequest(MAX_VALUE, 1L)));

        // expect
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 주문 항목이 있습니다");
    }

    @Test
    void 주문_테이블이_존재하지_않는다면_예외가_발생한다() {
        // given
        Menu menu = menuRepository.save(menu("메뉴", 10000L, null, List.of()));
        OrderRequest request = orderRequest(MAX_VALUE, List.of(orderLineItemRequest(menu.getId(), 1L)));

        // expect
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 테이블이 존재하지 않습니다");
    }

    @Test
    void 주문_테이블이_빈_테이블이면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTableRepository.save(orderTable(10, true));
        OrderTable savedOrderTable = orderTableRepository.save(orderTable(10, false));
        Order savedOrder = orderRepository.save(order(savedOrderTable.getId(), COOKING));
        Menu menu = menuRepository.save(menu("메뉴", 10000L, null, List.of()));
        OrderLineItem savedOrderLineItem = orderLineItemRepository.save(new OrderLineItem(savedOrder, menu.getId(), 10));
        OrderRequest orderRequest = orderRequest(orderTable.getId(), List.of(new OrderLineItemRequest(savedOrderLineItem.getMenuId(), savedOrderLineItem.getQuantity())));

        // expect
        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 테이블이 빈 테이블입니다");
    }

    @Test
    void 주문을_생성한다() {
        // given
        OrderTable savedOrderTable = orderTableRepository.save(orderTable(10, false));
        Order order = orderRepository.save(order(savedOrderTable.getId(), COOKING));
        Menu menu = menuRepository.save(menu("메뉴", 10000L, null, List.of()));
        OrderLineItem savedOrderLineItem = orderLineItemRepository.save(new OrderLineItem(order, menu.getId(), 10));
        OrderRequest request = orderRequest(savedOrderTable.getId(), List.of(new OrderLineItemRequest(savedOrderLineItem.getMenuId(), savedOrderLineItem.getQuantity())));

        // when
        Order savedOrder = orderService.create(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(savedOrder.getOrderStatus()).isEqualTo(COOKING);
            softly.assertThat(savedOrder.getOrderTableId()).isEqualTo(request.getOrderTableId());
            softly.assertThat(savedOrder.getOrderedTime()).isNotNull();
            softly.assertThat(savedOrder.getOrderLineItems()).map(OrderLineItem::getOrder)
                    .isNotNull();
        });
    }

    @Test
    void 주문을_조회한다() {
        // given
        Menu menu = menuRepository.save(menu("메뉴", 10000L, null, List.of()));

        OrderTable orderTable = orderTableRepository.save(orderTable(10, false));
        Order order1 = orderRepository.save(order(orderTable.getId(), MEAL));
        OrderLineItem orderLineItem1 = orderLineItemRepository.save(orderLineItem(order1, menu.getId(), 2L));
        order1.changeOrderLineItems(List.of(orderLineItem1));
        Order order2 = orderRepository.save(order(orderTable.getId(), COOKING));
        OrderLineItem orderLineItem2 = orderLineItemRepository.save(orderLineItem(order2, menu.getId(), 2L));
        order2.changeOrderLineItems(List.of(orderLineItem2));

        // when
        List<Order> orders = orderService.list();

        // then
        assertThat(orders)
                .usingRecursiveComparison()
                .isEqualTo(List.of(order1, order2));
    }

    @Test
    void 주문의_상태를_변경할_때_주문이_존재하지_않으면_예외가_발생한다() {
        // expect
        assertThatThrownBy(() -> orderService.changeOrderStatus(MAX_VALUE, new OrderChangeStatusRequest(COOKING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문이 존재하지 않습니다");
    }

    @Test
    void 주문의_상태를_변경할_때_상태가_완료면_예외가_발생한다() {
        // given
        Order order = order(1L, OrderStatus.COMPLETION);
        Order savedOrder = orderRepository.save(order);
        OrderChangeStatusRequest request = new OrderChangeStatusRequest(COOKING);

        // expect
        assertThatThrownBy(() -> orderService.changeOrderStatus(savedOrder.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("완료 상태의 주문은 변경할 수 없습니다");
    }

    @Test
    void 주문의_상태를_변경한다() {
        // given
        Order order = order(1L, COOKING);
        Order savedOrder = orderRepository.save(order);
        OrderChangeStatusRequest newOrder = new OrderChangeStatusRequest(OrderStatus.MEAL);

        // when
        Order changedOrder = orderService.changeOrderStatus(savedOrder.getId(), newOrder);

        // then
        assertSoftly(softly -> {
            softly.assertThat(changedOrder.getOrderStatus()).isEqualTo(OrderStatus.MEAL);
            softly.assertThat(changedOrder.getId()).isEqualTo(savedOrder.getId());
        });
    }
}
