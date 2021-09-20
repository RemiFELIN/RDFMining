package com.i3s.app.rdfminer.dbpedia;

import java.util.ArrayList;

public class Resource {

	String data;
	ArrayList<Resource> object;
	ArrayList<Resource> subject;

	Resource(String value) {
		data = value;
		object = null;
		subject = null;
	}

	void setData(String data) {
		this.data = data;
	}
	
	void setObject(ArrayList<Resource> object) {
		this.object = object;
	}

	void setSubject(ArrayList<Resource> subject) {
		this.subject = subject;
	}
	
	ArrayList<Resource> getObject() {
		return this.object;
	}

	ArrayList<Resource> getSubject() {
		return this.subject;
	}

	String getData() {
		return this.data;
	}
}
