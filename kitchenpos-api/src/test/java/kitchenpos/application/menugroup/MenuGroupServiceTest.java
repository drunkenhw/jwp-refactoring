package kitchenpos.application.menugroup;

import kitchenpos.application.MenuGroupService;
import kitchenpos.application.dto.MenuGroupRequest;
import kitchenpos.support.ServiceTest;
import kitchenpos.domain.menugroup.MenuGroup;
import kitchenpos.domain.menugroup.MenuGroupRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@ServiceTest
class MenuGroupServiceTest {

    @Autowired
    private MenuGroupService menuGroupService;
    @Autowired
    private MenuGroupRepository menuGroupRepository;

    @Test
    void 주문_그룹을_생성한다() {
        // given
        MenuGroupRequest menuGroup = new MenuGroupRequest("korean");

        // when
        MenuGroup savedMenuGroup = menuGroupService.create(menuGroup);

        // then
        assertSoftly(softly -> {
            softly.assertThat(savedMenuGroup.getName()).isEqualTo(menuGroup.getName());
            softly.assertThat(savedMenuGroup.getId()).isNotNull();
        });
    }

    @Test
    void 주문을_전체_조회한다() {
        // given
        menuGroupRepository.save(new MenuGroup("korean"));
        menuGroupRepository.save(new MenuGroup("french"));

        // when
        List<MenuGroup> savedMenuGroups = menuGroupService.list();

        // then
        assertThat(savedMenuGroups).map(MenuGroup::getName)
                .usingRecursiveComparison()
                .isEqualTo(List.of("korean", "french"));
    }
}
