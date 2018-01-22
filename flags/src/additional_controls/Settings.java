package additional_controls;

import java.util.Stack;

public class Settings
{
	private Stack<Double> storedValues = new Stack<>();
	private Stack<Boolean> storedOptions = new Stack<>();
	private String id;
	
	public Settings(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public double getNumeric()
	{
		return storedValues.pop();
	}
	
	public boolean getOption()
	{
		return storedOptions.pop();
	}
	
	public void add(double...values)
	{
		for (double x : values) storedValues.add(x);
	}
	
	public void add(boolean...options)
	{
		for (boolean b : options) storedOptions.add(b);
	}
	
	public boolean equals(Settings s)
	{
		return this.id.equals(s.getId());
	}
}
