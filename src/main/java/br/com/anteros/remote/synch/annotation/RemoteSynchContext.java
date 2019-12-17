package br.com.anteros.remote.synch.annotation;

import java.util.Properties;

import br.com.anteros.persistence.session.SQLSession;

public class RemoteSynchContext {
	
	private Properties props = new Properties();
	private SQLSession session;
	

	public RemoteSynchContext(SQLSession session) {
		super();
		this.session = session;
	}


	public void addParameter(String name, Object value) {
		props.put(name, value);		
	}
	
	
	public Object getParameter(String name) {
		return props.get(name);
	}


	public SQLSession getSession() {
		return session;
	}


	public void setSession(SQLSession session) {
		this.session = session;
	}

}
