package com.hemal.eventhub2.model;

/**
 * Created by Hemal on 11-Oct-16.
 */
public class Club
{
	public Integer clubID;
	public String name;
	public String alias;
	public boolean followed;

	@Override
	public String toString()
	{
		return name;
	}
}
