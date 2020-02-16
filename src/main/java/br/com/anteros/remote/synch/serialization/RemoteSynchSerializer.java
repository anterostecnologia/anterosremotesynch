package br.com.anteros.remote.synch.serialization;

import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.remote.synch.resource.ResultData;

public interface RemoteSynchSerializer {

	public <T> ObjectNode serialize(ResultData<T> data, SQLSession currentSession, Class<?> resultClass);
}
