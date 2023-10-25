package kitchenpos.application.menu;

import kitchenpos.application.dto.MenuRequest;
import kitchenpos.domain.menu.Menu;
import kitchenpos.domain.menu.MenuProduct;
import kitchenpos.domain.menu.MenuRepository;
import kitchenpos.domain.menugroup.MenuGroup;
import kitchenpos.domain.menugroup.MenuGroupRepository;
import kitchenpos.domain.product.Product;
import kitchenpos.domain.product.ProductRepository;
import kitchenpos.support.ServiceTest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@ServiceTest
class MenuServiceTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MenuGroupRepository menuGroupRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private MenuService menuService;

    @Test
    void 메뉴를_생성한다() {
        // Given
        Product product = productRepository.save(new Product("chicken", BigDecimal.valueOf(1000)));
        MenuGroup menuGroup = menuGroupRepository.save(new MenuGroup("menuGroup"));
        MenuRequest request = new MenuRequest("메뉴", BigDecimal.valueOf(10_000L), menuGroup.getId(), List.of(new MenuRequest.MenuProductRequest(product.getId(), 10L)));

        // When
        Menu createdMenu = menuService.create(request);

        // Then
        assertThat(createdMenu.getId()).isNotNull();
    }

    @Test
    void 메뉴_가격이_0보다_작으면_예외를_던진다() {
        // given
        MenuRequest request = new MenuRequest("메뉴 이름", BigDecimal.valueOf(-1L), 1L, List.of());

        // expect
        assertThatThrownBy(() -> menuService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 0원 이상이여야합니다");
    }

    @Test
    void 메뉴의_가격이_메뉴_상품들의_금액의_합보다_큰_경우_예외를_던진다() {
        // given
        Product product = productRepository.save(new Product("chicken", BigDecimal.valueOf(1000)));
        MenuGroup menuGroup = menuGroupRepository.save(new MenuGroup("menuGroup"));
        MenuProduct menuProduct = new MenuProduct(product.getId(), 1L);
        MenuRequest request = new MenuRequest("cheese pizza", BigDecimal.valueOf(10001), menuGroup.getId(), List.of(new MenuRequest.MenuProductRequest(product.getId(), 10L)));

        // expect
        assertThatThrownBy(() -> menuService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격의 합이 맞지 않습니다");
    }

    @Test
    void 메뉴_그룹_아이디에_해당하는_메뉴_그룹이_없는_경우_예외를_던진다() {
        // given
        MenuRequest request = new MenuRequest("메뉴 이름", BigDecimal.valueOf(1000L), Long.MAX_VALUE, List.of());

        // expect
        assertThatThrownBy(() -> menuService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메뉴 그룹이 존재 해야합니다");
    }

    @Test
    void 전체_메뉴를_조회할_수_있다() {
        // given
        Menu menu1 = menuRepository.save(new Menu("fried chicken", BigDecimal.valueOf(10000L), 1L, List.of()));
        Menu menu2 = menuRepository.save(new Menu("spicy chicken", BigDecimal.valueOf(20000L), 1L, List.of()));

        // when
        List<Menu> result = menuService.list();

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(List.of(menu1, menu2));
    }
}
