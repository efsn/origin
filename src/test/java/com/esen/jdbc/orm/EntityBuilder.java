package com.esen.jdbc.orm;

import java.util.Date;

/**
 * 测试  BeanBuilder 所需的类
 */
public class EntityBuilder {
	public Entity createEntity(int id, String name) {
		return new Entity(id, name + name, 0, new Date());
	}
}
