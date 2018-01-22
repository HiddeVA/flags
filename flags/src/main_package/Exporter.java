package main_package;

import java.io.*;
import flags.*;

public class Exporter 
{
	static String currentDirectory = "C:\\Users\\Public\\";
	
	private Exporter() {}
	
	public static String getDirectory()
	{
		return currentDirectory;
	}
	
	public static void setExportDestination(File file)
	{
		currentDirectory = file.isDirectory() ? file.getAbsolutePath() : file.getParent();
	}
	
	public static String generateSVG(Flag flag, String countryname)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" "
				+ "height=\"" + flag.getHeight() + "\" width=\"" + flag.getWidth() + "\">"); //metadata
		sb.append(flag.getSVGData()); //actual data
		sb.append("</svg>"); //closing tag
		return sb.toString();
	}
	
	public static void exportAsSVGFile(Flag flag, String countryname)
	{
		File flagfile = new File(currentDirectory + countryname + ".svg");
		PrintWriter pw;
		try 
		{
			pw = new PrintWriter(new BufferedWriter(new FileWriter(flagfile)));
			
		} 
		catch (IOException e)
		{
			System.out.println("Unable to find file");
			return;
		}
		pw.append("<svg xmlns=\"http://www.w3.org/2000/svg\" "
				+ "height=\"" + flag.getHeight() + "\" width=\"" + flag.getWidth() + "\">"); //metadata
		pw.append(flag.getSVGData()); //actual data
		pw.append("</svg>"); //closing tag
		pw.close();
	}
}
