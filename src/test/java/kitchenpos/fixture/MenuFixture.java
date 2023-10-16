package kitchenpos.fixture;

import kitchenpos.application.dto.MenuRequest;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.application.dto.MenuRequest.MenuProductRequest;

public class MenuFixture {

    public static Menu menu(String name, Long price, Long menuGroupId, List<MenuProduct> menuProducts) {
        return new Menu(null, name, BigDecimal.valueOf(price), menuGroupId, menuProducts);
    }

    public static MenuRequest menuRequest(String name, Long price, Long menuGroupId, List<MenuProduct> menuProducts) {
        List<MenuProductRequest> requests = menuProducts.stream()
                .map(it -> new MenuProductRequest(it.getProduct().getId(), it.getQuantity()))
                .collect(Collectors.toList());
        return new MenuRequest(name, BigDecimal.valueOf(price), menuGroupId, requests);
    }
}
