package com.itsonin.android.entity;

/**
 * @author nkislitsin
 *
 */
public class Counter {
	
	private java.lang.String name;
	private long value;
	
	@SuppressWarnings("unused")
	private Counter(){
	}
	
	public Counter(String name, long value){
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
	
}
