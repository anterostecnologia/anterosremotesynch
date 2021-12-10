package br.com.anteros.remote.synch.annotation;

import br.com.anteros.remote.synch.resource.MobileResultData;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface MobileFilterData <T> {

	public MobileResultData<T> execute(RemoteSynchContext context);

	public void processTransientFields(T entity, ObjectNode node);
}
