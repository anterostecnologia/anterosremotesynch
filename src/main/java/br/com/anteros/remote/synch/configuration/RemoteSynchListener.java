package br.com.anteros.remote.synch.configuration;

import br.com.anteros.remote.synch.annotation.RemoteSynchContext;

public interface RemoteSynchListener {
	
	void onConfirmDataIntegration(RemoteSynchContext context);

}
