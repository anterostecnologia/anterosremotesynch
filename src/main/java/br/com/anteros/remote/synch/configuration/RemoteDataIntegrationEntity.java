package br.com.anteros.remote.synch.configuration;

import br.com.anteros.persistence.metadata.EntityCache;

public class RemoteDataIntegrationEntity {

	private Object name;
	private EntityCache[] entityCache;

	public RemoteDataIntegrationEntity(String name, EntityCache[] entityCache) {
		this.name = name;
		this.entityCache = entityCache;
	}
	

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public EntityCache[] getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(EntityCache[] entityCache) {
		this.entityCache = entityCache;
	}

}
