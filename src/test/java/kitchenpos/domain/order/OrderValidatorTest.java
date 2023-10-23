package kitchenpos.domain.order;

import kitchenpos.domain.table.OrderTable;
import kitchenpos.domain.table.OrderTableRepository;
import kitchenpos.support.ServiceTest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static kitchenpos.fixture.OrderLineItemFixture.orderLineItem;
import static kitchenpos.fixture.OrderTableFixtrue.orderTable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@ServiceTest
class OrderValidatorTest {
    @Autowired
    private OrderTableRepository orderTableRepository;
    @Autowired
    private OrderValidator orderValidator;

    @Test
    void 주문은_주문_테이블이_빈_테이블이면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTable(10, true);
        orderTableRepository.save(orderTable);

        // expect
        assertThatThrownBy(() -> Order.createWithoutId(orderTable.getId(), List.of(orderLineItem(1L, 10)), orderValidator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 테이블이 빈 테이블입니다");
    }
}
