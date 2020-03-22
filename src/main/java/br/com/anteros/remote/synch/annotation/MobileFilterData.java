package br.com.anteros.remote.synch.annotation;

import br.com.anteros.remote.synch.resource.ResultData;

public interface MobileFilterData <T> {

	public ResultData<T> execute(RemoteSynchContext context);
}
