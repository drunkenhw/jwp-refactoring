package kitchenpos.application;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.fake.InMemoryMenuDao;
import kitchenpos.fake.InMemoryMenuGroupDao;
import kitchenpos.fake.InMemoryMenuProductDao;
import kitchenpos.fake.InMemoryProductDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class MenuServiceTest {

    private MenuGroupDao menuGroupDao;
    private MenuDao menuDao;
    private MenuProductDao menuProductDao;
    private ProductDao productDao;
    private MenuService menuService;
    private MenuGroup savedMenuGroup;
    private MenuProduct savedMenuProduct;

    @BeforeEach
    void before() {
        menuGroupDao = new InMemoryMenuGroupDao();
        menuDao = new InMemoryMenuDao();
        menuProductDao = new InMemoryMenuProductDao();
        productDao = new InMemoryProductDao();
        menuService = new MenuService(menuDao, menuGroupDao, menuProductDao, productDao);
        savedMenuGroup = menuGroupDao.save(new MenuGroup("메뉴 그룹"));
        savedMenuProduct = menuProductDao.save(new MenuProduct());
    }

    @Test
    void 메뉴를_생성한다() {
        // Given
        Product product = productDao.save(new Product("chicken", BigDecimal.valueOf(1_000)));
        MenuProduct menuProduct = new MenuProduct(1L, product.getId(), 10);
        MenuGroup menuGroup = menuGroupDao.save(new MenuGroup("메뉴 그룹"));
        Menu menu = new Menu("메뉴", BigDecimal.valueOf(10_000), menuGroup.getId(), List.of(menuProduct));

        // When
        Menu createdMenu = menuService.create(menu);

        // Then
        assertThat(createdMenu.getId()).isNotNull();
    }

    @Test
    void 메뉴_가격이_0보다_작으면_예외를_던진다() {
        // given
        Menu menu = new Menu("메뉴 이름", BigDecimal.valueOf(-1), savedMenuGroup.getId(), List.of(savedMenuProduct));

        // expect
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메뉴_그룹_아이디에_해당하는_메뉴_그룹이_없는_경우_예외를_던진다() {
        // given
        Menu menu = new Menu("메뉴 이름", BigDecimal.valueOf(1000), Long.MAX_VALUE, List.of(savedMenuProduct));

        // expect
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 가격이_실제_메뉴_상품들의_총_가격보다_크면_예외를_던진다() {
        // given
        Menu menu = new Menu("메뉴 이름", BigDecimal.valueOf(2001), savedMenuGroup.getId(), List.of(savedMenuProduct));

        // expect
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 전체_메뉴를_조회할_수_있다() {
        // given
        Product product = productDao.save(new Product("chicken", BigDecimal.valueOf(1_000)));
        MenuProduct menuProduct = new MenuProduct(0L, product.getId(), 10);
        MenuGroup menuGroup = menuGroupDao.save(new MenuGroup("메뉴 그룹"));
        Menu menu = new Menu("메뉴", BigDecimal.valueOf(10_000), menuGroup.getId(), List.of(menuProduct));

        menuService.create(menu);

        // when
        List<Menu> menus = menuService.list();

        // then
        assertThat(menus).hasSize(1);
    }
}
