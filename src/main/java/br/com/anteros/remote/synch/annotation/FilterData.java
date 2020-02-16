package br.com.anteros.remote.synch.annotation;

import br.com.anteros.remote.synch.resource.ResultData;

public interface FilterData <T> {

	public ResultData<T> execute(RemoteSynchContext context);
}
