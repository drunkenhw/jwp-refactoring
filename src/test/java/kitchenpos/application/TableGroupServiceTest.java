package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.fake.InMemoryOrderDao;
import kitchenpos.fake.InMemoryOrderTableDao;
import kitchenpos.fake.InMemoryTableGroupDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class TableGroupServiceTest {

    private TableGroupService tableGroupService;
    private OrderDao orderDao;
    private OrderTableDao orderTableDao;
    private TableGroupDao tableGroupDao;

    @BeforeEach
    void before() {
        orderDao = new InMemoryOrderDao();
        orderTableDao = new InMemoryOrderTableDao();
        tableGroupDao = new InMemoryTableGroupDao();
        tableGroupService = new TableGroupService(orderDao, orderTableDao, tableGroupDao);
    }

    @Test
    void 단체_지정을_생성한다() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(null, 10, true));
        OrderTable orderTable2 = orderTableDao.save(new OrderTable(null, 3, true));
        TableGroup tableGroup = new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2));

        // when
        TableGroup savedTableGroup = tableGroupService.create(tableGroup);

        // then
        assertThat(savedTableGroup).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(tableGroup);
    }

    @Test
    void 단체_지정을_생성하면_주문이_단체_지정되고_테이블의_상태가_주문_테이블로_변경된다() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(null, 10, true));
        OrderTable orderTable2 = orderTableDao.save(new OrderTable(null, 3, true));
        TableGroup tableGroup = new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2));

        // when
        tableGroupService.create(tableGroup);

        // then
        OrderTable savedOrderTable = orderTableDao.findById(orderTable.getId()).get();
        OrderTable savedOrderTable2 = orderTableDao.findById(orderTable2.getId()).get();
        assertSoftly(softly -> {
            softly.assertThat(savedOrderTable.getTableGroupId()).isNotNull();
            softly.assertThat(savedOrderTable2.getTableGroupId()).isNotNull();
            softly.assertThat(savedOrderTable.isEmpty()).isFalse();
            softly.assertThat(savedOrderTable2.isEmpty()).isFalse();
        });
    }

    @Test
    void 단체_지정을_생성할_때_주문_테이블이_1개_이하면_예외가_발생한다() {
        // given
        TableGroup tableGroup = new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of());

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 단체_지정을_생성할_때_저장되어있는_주문_테이블의_개수와_다르면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(null, 10, true));
        OrderTable orderTable2 = new OrderTable(null, 3, true);
        TableGroup tableGroup = new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2));

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 단체_지정을_생성할_때_빈_테이블이지_않으면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(null, 10, false));
        OrderTable orderTable2 = orderTableDao.save(new OrderTable(null, 3, false));
        TableGroup tableGroup = new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2));

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 단체_지정을_생성할_때_이미_단체_지정이_되어있으면_예외가_발생한다() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(1L, 10, true));
        OrderTable orderTable2 = orderTableDao.save(new OrderTable(1L, 3, true));
        TableGroup tableGroup = new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2));

        // expect
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 단체_지정을_해제한다() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(0L, 10, false));
        OrderTable orderTable2 = orderTableDao.save(new OrderTable(0L, 3, false));
        orderDao.save(new Order(orderTable.getId(), OrderStatus.COMPLETION.name(), LocalDateTime.of(2002, 3, 3, 3, 3)));
        TableGroup tableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2)));

        // when
        tableGroupService.ungroup(tableGroup.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(orderTableDao.findById(orderTable.getId())).isPresent();
            softly.assertThat(orderTableDao.findById(orderTable.getId()).get().getTableGroupId()).isNull();
            softly.assertThat(orderTableDao.findById(orderTable.getId()).get().isEmpty()).isFalse();
            softly.assertThat(orderTableDao.findById(orderTable2.getId())).isPresent();
            softly.assertThat(orderTableDao.findById(orderTable2.getId()).get().getTableGroupId()).isNull();
            softly.assertThat(orderTableDao.findById(orderTable2.getId()).get().isEmpty()).isFalse();
        });
    }

    @EnumSource(value = OrderStatus.class, names = {"COOKING", "MEAL"})
    @ParameterizedTest
    void 단체_지정을_해제할_때_주문의_상태가_조리_혹은_식사_이면_예외가_발생한다(OrderStatus orderStatus) {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(0L, 10, false));
        OrderTable orderTable2 = orderTableDao.save(new OrderTable(0L, 3, false));
        orderDao.save(new Order(orderTable.getId(), orderStatus.name(), LocalDateTime.of(2002, 3, 3, 3, 3)));
        TableGroup tableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.of(2002, 3, 3, 3, 3), List.of(orderTable, orderTable2)));

        // when
        assertThatThrownBy(() -> tableGroupService.ungroup(tableGroup.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
