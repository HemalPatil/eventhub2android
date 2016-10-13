package com.hemal.eventhub2.model;

/**
 * Created by Hemal on 09-Oct-16.
 */
public class Event {

	public int id;
	public String eventName;
	public String eventVenue;
	public String imagePath;
	public boolean followed = false;

	private String eventTime;

	public Event()
	{

	}

	public Event(int id, String eventName, String eventTime, String eventVenue, String imagePath)
	{
		this.id=id;
		this.eventName=eventName;
		this.eventTime=eventTime;
		this.eventVenue =eventVenue;
		this.imagePath=imagePath;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int num)
	{
		this.id=num;
	}

	public String getEventName()
	{
		return eventName;
	}

	public void setEventName(String name)
	{
		this.eventName=name;
	}

	public String getEventTime()
	{
		return eventTime;
	}

	public void setEventTime(String time)
	{
		this.eventTime=time;
	}

	public String getEventVenue()
	{
		return eventVenue;
	}

	public void setEventVenue(String name)
	{
		this.eventVenue =name;
	}

	public String getImagePath(){return imagePath;}

	public void setImagePath(String name)
	{
		this.imagePath=name;
	}

}
