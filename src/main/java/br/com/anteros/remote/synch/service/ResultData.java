package br.com.anteros.remote.synch.service;

import java.util.List;

public class ResultData <T> {
	
	private String name;
	
	private List<T> content;
	
	private Class<?> resultClass;
	

	private ResultData(String name, List<T> content, Class<?> resultClass) {
		super();
		this.name = name;
		this.content = content;
		this.resultClass = resultClass;
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
	
	public static <T> ResultData<T> of(String name, List<T> content, Class<?> resultClass) {
		return new ResultData<T>(name, content, resultClass);
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public void setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
	}

}
