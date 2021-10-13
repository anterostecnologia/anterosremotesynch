package br.com.anteros.remote.synch.resource;

import java.util.List;
import java.util.Map;

public interface TransactionListener {

    public void onFinishTransaction(List<TransactionHistoryData> history, Map<String, Object> cache);
}
