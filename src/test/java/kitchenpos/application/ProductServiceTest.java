package kitchenpos.application;

import kitchenpos.application.dto.ProductRequest;
import kitchenpos.domain.Product;
import kitchenpos.domain.ProductRepository;
import kitchenpos.fake.InMemoryProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class ProductServiceTest {

    private ProductService productService;
    private ProductRepository productRepository;

    @BeforeEach
    void before() {
        productRepository = new InMemoryProductRepository();
        productService = new ProductService(productRepository);
    }

    @Test
    void 상품을_생성한다() {
        // given
        ProductRequest chicken = new ProductRequest("chicken", BigDecimal.valueOf(10_000));

        // when
        Product savedProduct = productService.create(chicken);

        // then
        assertSoftly(softly -> {
            softly.assertThat(savedProduct).usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(chicken);
            softly.assertThat(savedProduct.getId()).isNotNull();
        });
    }

    @Test
    void 상품을_생성할_때_상품_가격이_없으면_예외가_발생한다() {
        // given
        ProductRequest invalidProduct = new ProductRequest("name", null);

        // expect
        assertThatThrownBy(() -> productService.create(invalidProduct))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품을_생성할_때_상품_가격이_음수면_예외가_발생한다() {
        // given
        ProductRequest invalidProduct = new ProductRequest("chicken", BigDecimal.valueOf(-1));

        // expect
        assertThatThrownBy(() -> productService.create(invalidProduct))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
