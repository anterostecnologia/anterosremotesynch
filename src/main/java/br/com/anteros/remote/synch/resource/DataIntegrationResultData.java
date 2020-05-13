package br.com.anteros.remote.synch.resource;

import java.util.Date;
import java.util.List;

public class DataIntegrationResultData <T> {
	
	private String name;
	
	private Date dhSynch = new Date();
	
	private List<T> content;
	
	private DataIntegrationResultData(String name, List<T> content) {
		super();
		this.name = name;
		this.content = content;
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
	
	
	public static DataIntegrationResultData of(String name, List content) {
		return new DataIntegrationResultData(name, content);
	}

	public Date getDhSynch() {
		return dhSynch;
	}

	public void setDhSynch(Date dhSynch) {
		this.dhSynch = dhSynch;
	}
	
}
