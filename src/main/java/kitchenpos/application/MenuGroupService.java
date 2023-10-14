package kitchenpos.application;

import kitchenpos.application.dto.MenuGroupRequest;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.domain.MenuGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuGroupService {
    private final MenuGroupDao menuGroupDao;

    public MenuGroupService(final MenuGroupDao menuGroupDao) {
        this.menuGroupDao = menuGroupDao;
    }

    @Transactional
    public MenuGroup create(final MenuGroupRequest request) {
        MenuGroup menuGroup = new MenuGroup(request.getName());
        return menuGroupDao.save(menuGroup);
    }

    public List<MenuGroup> list() {
        return menuGroupDao.findAll();
    }
}
