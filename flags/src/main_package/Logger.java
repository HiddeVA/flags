package main_package;

import java.io.*;

public class Logger 
{
	private static File logFile = new File("C:\\Users\\Public\\flagproject_log.txt");
	
	public static void logToConsole(String...message)
	{
		for (String m : message) {
			System.out.println(m);
		}
	}
	
	public static void logToConsole(Object...message)
	{
		for (Object o : message) {
			System.out.println(o.toString());
		}
	}
	
	public static void logToFile(String message)
	{
		try 
		{
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			pw.println(message);
			pw.close();
		} 
		catch (IOException e)
		{
			System.out.println("Unable to find logfile");
		}
	}
}
