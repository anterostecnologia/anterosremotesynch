package br.com.anteros.remote.synch.configuration;

import br.com.anteros.remote.synch.annotation.DataIntegrationFilterData;

public class RemoteFilterDataIntegrationEntity {

	private Object name;
	private DataIntegrationFilterData filterData;

	public RemoteFilterDataIntegrationEntity(String name, DataIntegrationFilterData filterData) {
		this.name = name;
		this.filterData = filterData;
	}

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public DataIntegrationFilterData getFilterData() {
		return filterData;
	}

	public void setFilterData(DataIntegrationFilterData filterData) {
		this.filterData = filterData;
	}

}