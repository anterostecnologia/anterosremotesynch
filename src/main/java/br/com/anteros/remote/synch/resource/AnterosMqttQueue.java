package br.com.anteros.remote.synch.resource;

public interface AnterosMqttQueue {

    public void queueItem(TransactionInfo tinfo);
}
