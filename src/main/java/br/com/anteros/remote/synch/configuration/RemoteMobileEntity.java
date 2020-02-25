package br.com.anteros.remote.synch.configuration;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.remote.synch.annotation.FilterData;

public class RemoteMobileEntity {

	private Object name;
	private EntityCache entityCache;
	private FilterData filterData;

	public RemoteMobileEntity(String name, EntityCache entityCache) {
		this.name = name;
		this.entityCache = entityCache;
	}
	
	public RemoteMobileEntity(String name, EntityCache entityCache, FilterData filterData) {
		this.name = name;
		this.entityCache = entityCache;
		this.filterData = filterData;
	}

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public FilterData getFilterData() {
		return filterData;
	}

	public void setFilterData(FilterData filterData) {
		this.filterData = filterData;
	}
	
	

}
