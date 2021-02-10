package br.com.anteros.remote.synch.annotation;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.remote.synch.configuration.RemoteSynchManager.RemoteRecord;

public interface DataIntegrationPostProcessor {

	public void afterInsertRecord(SQLSession session, RemoteRecord record);
	public void afterDeleteRecord(SQLSession session, RemoteRecord record);
	public void afterUpdateRecord(SQLSession session, RemoteRecord record);
}
