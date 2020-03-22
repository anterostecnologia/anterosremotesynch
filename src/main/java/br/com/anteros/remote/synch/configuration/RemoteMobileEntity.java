package br.com.anteros.remote.synch.configuration;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.remote.synch.annotation.MobileDataProcessor;
import br.com.anteros.remote.synch.annotation.MobileFilterData;

public class RemoteMobileEntity {

	private Object name;
	private EntityCache entityCache;
	private MobileFilterData filterData;
	private MobileDataProcessor dataProcessor;

	public RemoteMobileEntity(String name, EntityCache entityCache) {
		this.name = name;
		this.entityCache = entityCache;
	}
	
	public RemoteMobileEntity(String name, EntityCache entityCache, MobileFilterData filterData) {
		this.name = name;
		this.entityCache = entityCache;
		this.filterData = filterData;
	}
	
	public RemoteMobileEntity(String name, EntityCache entityCache, MobileFilterData filterData, MobileDataProcessor dataProcessor) {
		this.name = name;
		this.entityCache = entityCache;
		this.filterData = filterData;
		this.dataProcessor = dataProcessor;
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

	public MobileFilterData getFilterData() {
		return filterData;
	}

	public void setFilterData(MobileFilterData filterData) {
		this.filterData = filterData;
	}

	public MobileDataProcessor getDataProcessor() {
		return dataProcessor;
	}

	public void setDataProcessor(MobileDataProcessor dataProcessor) {
		this.dataProcessor = dataProcessor;
	}

}
