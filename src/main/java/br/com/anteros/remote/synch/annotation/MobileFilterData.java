package br.com.anteros.remote.synch.annotation;

import br.com.anteros.remote.synch.resource.MobileResultData;

public interface MobileFilterData <T> {

	public MobileResultData<T> execute(RemoteSynchContext context);
}
