package org.my.heart.dao;

import org.my.heart.entity.menu.MenuView;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

public interface MenuViewRepository extends Repository<MenuView, Long>, JpaSpecificationExecutor<MenuView> {

}
