package br.com.anteros.remote.synch.configuration;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.remote.synch.annotation.DataIntegrationFilterData;


public class RemoteDataIntegrationEntity {

	private Object name;
	private EntityCache[] entityCache;
	private DescriptionField descriptionField;
	
	public RemoteDataIntegrationEntity(String name) {
		this.name = name;
	}

	public RemoteDataIntegrationEntity(String name, EntityCache[] entityCache) {
		this.name = name;
		this.entityCache = entityCache;
	}
	

	public RemoteDataIntegrationEntity(String name, DescriptionField descriptionField) {
		this.name = name;
		this.descriptionField = descriptionField;
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


	public DescriptionField getDescriptionField() {
		return descriptionField;
	}

	public void setDescriptionField(DescriptionField descriptionField) {
		this.descriptionField = descriptionField;
	}

}