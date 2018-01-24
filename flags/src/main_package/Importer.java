package main_package;

import java.io.*;
import java.util.*;
import javafx.scene.paint.Color;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import flags.*;

public class Importer 
{
	private Importer(){}
	
	private static File importFile;
	
	public static void setImportFile(File f)
	{
		importFile = f;
	}
	
	public static Flag beginImport()
	{
		if (importFile == null || importFile.isDirectory()) return null;
		String filepath = importFile.getAbsolutePath();
		String fileExtension = filepath.substring(filepath.length()-3);
		switch(fileExtension)
		{
		case "svg": return beginImportSVG();
		case "xml": return beginImportXML();
		default: return null;
		}
	}
	
	private static Flag beginImportSVG()
	{
		System.out.println("Not implemented yet");
		return null;
	}
	
	private static Flag beginImportXML()
	{		
		SAXBuilder saxbuilder = new SAXBuilder();
		Document document;
		try
		{
			document = saxbuilder.build(importFile);
		} 
		catch (JDOMException e)
		{
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
		
		Element baseElement = document.getRootElement();
		String flagtype = baseElement.getAttributeValue("type");
		if (flagtype.equals("Vertical") || flagtype.equals("Horizontal"))
		{
			List<Element> rows = baseElement.getChildren("row");
			List<Color> colourlist = new ArrayList<>();
			for (Element m : rows)
			{
				colourlist.add(FlagManager.getColour(m.getAttributeValue("colour")));
			}
			if (flagtype.equals("Vertical"))
			{
				return new VerticalFlag(colourlist);
			}
			else return new HorizontalFlag(colourlist);
		}
		return null;
	}
}
