package br.com.anteros.remote.synch.serialization;

import br.com.anteros.remote.synch.annotation.MobileFilterData;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.remote.synch.resource.MobileResultData;

public interface RemoteSynchSerializer {

	public <T> ObjectNode serialize(MobileFilterData<T> filterData, MobileResultData<T> data, SQLSession currentSession, Class<?> resultClass);
}
