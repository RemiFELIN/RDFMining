package com.i3s.app.rdfminer.dbpedia;

import java.util.ArrayList;

public class Resource1 {

	String Data;
	ArrayList<Resource1> Object;
	ArrayList<Resource1> Subject;

	Resource1(String value) {
		Data = value;
		Object = null;
		Subject = null;
	}

	ArrayList<Resource1> getObject() {
		return Object;
	}

	ArrayList<Resource1> getSubject() {
		return this.Subject;
	}

	String getData() {
		return this.Data;
	}

	void setObject(ArrayList<Resource1> object) {
		this.Object = object;
	}

	void setSubject(ArrayList<Resource1> subject) {
		this.Subject = subject;
	}
}
