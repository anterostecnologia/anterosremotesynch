package br.com.anteros.remote.synch.resource;

import java.util.List;
import java.util.Map;

public class ResultData <T> {
	
	private String name;
	
	private List<T> content;
	
	private Map<String,String> idsToRemove;
	
	private Class<?> resultClass;
	

	private ResultData(String name, List<T> content, Class<?> resultClass, Map<String,String> idsToRemove) {
		super();
		this.name = name;
		this.content = content;
		this.resultClass = resultClass;
		this.idsToRemove = idsToRemove;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}
	
	public static <T> ResultData<T> of(String name, List<T> content, Class<?> resultClass, Map<String,String> idsToRemove) {
		return new ResultData<T>(name, content, resultClass, idsToRemove);
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public void setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
	}

	public Map<String,String> getIdsToRemove() {
		return idsToRemove;
	}

	public void setIdsToRemove(Map<String,String> idsToRemove) {
		this.idsToRemove = idsToRemove;
	}

}
