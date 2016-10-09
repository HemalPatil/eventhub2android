package com.hemal.eventhub2.model;

/**
 * Created by Hemal on 09-Oct-16.
 */
public class Person {
	String name;

	public Person()
	{

	}

	public Person(String name)
	{
		this.name=name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}
}