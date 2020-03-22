package br.com.anteros.remote.synch.annotation;

import com.fasterxml.jackson.databind.JsonNode;

public interface MobileDataProcessor<T> {
	
	public long process(String clientId, String transactionId, JsonNode node);

}
