package br.com.anteros.remote.synch.annotation;

import br.com.anteros.remote.synch.resource.DataIntegrationResultData;

public interface DataIntegrationFilterData <T> {

	public DataIntegrationResultData<T> execute(RemoteSynchContext context);
}
