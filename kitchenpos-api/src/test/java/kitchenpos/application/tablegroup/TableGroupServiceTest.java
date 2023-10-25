package kitchenpos.application.tablegroup;

import kitchenpos.application.TableGroupService;
import kitchenpos.application.dto.TableGroupRequest;
import kitchenpos.application.dto.TableGroupResponse;
import kitchenpos.support.ServiceTest;
import kitchenpos.domain.order.Order;
import kitchenpos.domain.order.OrderLineItem;
import kitchenpos.domain.order.OrderRepository;
import kitchenpos.domain.order.OrderStatus;
import kitchenpos.domain.table.OrderTable;
import kitchenpos.domain.table.OrderTableRepository;
import kitchenpos.domain.tablegroup.TableGroup;
import kitchenpos.domain.tablegroup.TableGroupRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static kitchenpos.application.dto.TableGroupRequest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@ServiceTest
class TableGroupServiceTest {
    @Autowired
    private TableGroupService tableGroupService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTableRepository orderTableRepository;
    @Autowired
    private TableGroupRepository tableGroupRepository;

    @Test
    void 단체_지정을_생성한다() {
        // given
        OrderTable orderTable = orderTableRepository.save(new OrderTable(null, 10, true));
        OrderTable orderTable2 = orderTableRepository.save(new OrderTable(null, 3, true));
        TableGroupRequest tableGroup = new TableGroupRequest(List.of(new OrderTableIdRequest(orderTable.getId()), new OrderTableIdRequest(orderTable2.getId())));

        // when
        TableGroupResponse response = tableGroupService.create(tableGroup);

        // then
        assertThat(response.getId()).isNotNull();
    }

    @Test
    void 단체_지정을_생성하면_주문이_단체_지정되고_테이블의_상태가_주문_테이블로_변경된다() {
        // given
        OrderTable orderTable = orderTableRepository.save(new OrderTable(null, 10, true));
        OrderTable orderTable2 = orderTableRepository.save(new OrderTable(null, 3, true));
        TableGroupRequest tableGroup = new TableGroupRequest(List.of(new OrderTableIdRequest(orderTable.getId()), new OrderTableIdRequest(orderTable2.getId())));

        // when
        TableGroupResponse savedTableGroup = tableGroupService.create(tableGroup);

        // then
        assertThat(savedTableGroup.getId()).isNotNull();
    }

    @Test
    void 단체_지정을_생성할_때_주문_테이블이_1개_이하면_예외가_발생한다() {
        // given
        TableGroupRequest tableGroup = new TableGroupRequest(List.of());

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 테이블은 2개 이상이여야 합니다");
    }

    @Test
    void 단체_지정을_생성할_때_저장되어있는_주문_테이블의_개수와_다르면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTableRepository.save(new OrderTable(10, true));
        TableGroupRequest tableGroup = new TableGroupRequest(List.of(new OrderTableIdRequest(orderTable.getId()), new OrderTableIdRequest(Long.MAX_VALUE)));

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 테이블의 개수와 맞지 않습니다");
    }

    @Test
    void 단체_지정을_생성할_때_빈_테이블이지_않으면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTableRepository.save(new OrderTable(10, false));
        OrderTable orderTable2 = orderTableRepository.save(new OrderTable(3, false));
        TableGroupRequest tableGroup = new TableGroupRequest(List.of(new OrderTableIdRequest(orderTable.getId()), new OrderTableIdRequest(orderTable2.getId())));

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("단체 지정은 빈 테이블만 가능합니다");
    }

    @Test
    void 단체_지정을_생성할_때_이미_단체_지정이_되어있으면_예외가_발생한다() {
        // given
        TableGroup tableGroup = tableGroupRepository.save(new TableGroup(LocalDateTime.now()));
        OrderTable orderTable1 = orderTableRepository.save(new OrderTable(tableGroup.getId(), 10, true));
        OrderTable orderTable2 = orderTableRepository.save(new OrderTable(tableGroup.getId(), 10, true));
        TableGroupRequest request = new TableGroupRequest(List.of(new OrderTableIdRequest(orderTable1.getId()), new OrderTableIdRequest(orderTable2.getId())));

        // expect
        assertThatThrownBy(() -> tableGroupService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("단체 지정은 빈 테이블만 가능합니다");
    }

    @Test
    void 단체_지정을_해제한다() {
        // given
        TableGroup tableGroup = tableGroupRepository.save(new TableGroup(LocalDateTime.now()));
        orderTableRepository.save(new OrderTable(tableGroup.getId(), 0, true));
        orderTableRepository.save(new OrderTable(tableGroup.getId(), 0, true));

        // when
        tableGroupService.ungroup(tableGroup.getId());

        // then
        assertThat(orderTableRepository.findAllByTableGroupId(tableGroup.getId())).isEmpty();
    }

    @EnumSource(value = OrderStatus.class, names = {"COOKING", "MEAL"})
    @ParameterizedTest
    void 단체_지정을_해제할_때_주문의_상태가_조리_혹은_식사_이면_예외가_발생한다(OrderStatus orderStatus) {
        // given
        TableGroup tableGroup = tableGroupRepository.save(new TableGroup(LocalDateTime.now()));
        OrderTable orderTable = orderTableRepository.save(new OrderTable(tableGroup.getId(), 10, false));
        orderRepository.save(new Order(orderTable.getId(), orderStatus, List.of(new OrderLineItem(1L, 10))));

        // when
        assertThatThrownBy(() -> tableGroupService.ungroup(tableGroup.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 상태가 완료가 아닙니다");
    }
}
