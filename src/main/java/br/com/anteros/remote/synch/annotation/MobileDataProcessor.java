package br.com.anteros.remote.synch.annotation;

public interface MobileDataProcessor<T> {
	
	public DataProcessorResult process(RemoteSynchContext context);

}
