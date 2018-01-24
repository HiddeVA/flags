package main_package;

import flags.*;
import java.io.File;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

public class FlagManager
{
	private List<String> flags = new ArrayList<String>();
	CRUD crud;
	
	private static Map<String, Integer> dbColumnLabels = new HashMap<>();
	static {
	dbColumnLabels.put("symboltype", 2);
	dbColumnLabels.put("colour", 3);
	dbColumnLabels.put("size", 4);
	dbColumnLabels.put("xpos", 5);
	dbColumnLabels.put("ypos", 6);
	dbColumnLabels.put("orientation", 7);
	dbColumnLabels.put("whratio", 7);
	}
	
	public FlagManager(Connection conn)
	{
		if (conn == null)
		{
			flags.add("No Connection Available");
		}
		else
		{
			crud = new CRUD(conn);
		}
	}
	
	public FlagManager(String filepath)
	{
		this(new File(filepath));
	}
	
	public FlagManager(File flagfile)
	{
		System.out.println("This function is unavailable");
	}
	
	public boolean checkForCountryName(String s)
	{
		return flags.contains(s);
	}
	
	private void fillFlagList()
	{
		String[] tablename = {"flag", "country"};
		String field = "country.name";
		String condition = "ORDER BY country.name";
		if (crud != null)
		{
			flags = crud.selectMulti(tablename, field, condition);
			if (flags.size() == 0)
			{
				flags.add("No connection available");
			}
		}
		else
		{
			flags.clear();
			flags.add("No Connection Available");
		}
	}
	
	public List<String> getAllFlagsByCountry()
	{
		//Calling this function also refreshes the flag list
		fillFlagList();
		return this.flags;
	}
	
	private void getAllFlagSymbols(Flag flag, String countryname)
	{
		String[] tablenames = {"flagsymbol", "flag", "country"};		
		List<String[]> symboldata = crud.selectAll(tablenames, 
				"AND country.name = '" + countryname + "' AND symboltype IN ('Circle', 'Star', 'Crescent')");
		
		for (String[] s : symboldata)
		{
			flag.addSymbol(getSymbol(s[dbColumnLabels.get("symboltype")]),
					getColour(s[dbColumnLabels.get("colour")]),
					Double.parseDouble(s[dbColumnLabels.get("xpos")]),
					Double.parseDouble(s[dbColumnLabels.get("ypos")]),
					Double.parseDouble(s[dbColumnLabels.get("size")]),
					Double.parseDouble(s[dbColumnLabels.get("orientation")]));
		}
	}
	
	private void getAllBlocks(BlockFlag flag, String countryname)
	{
		List<String[]> flagBlockData = new ArrayList<String[]>();
		String[] tablenames = {"flagsymbol", "flag", "country"};
		flagBlockData = crud.selectAll(tablenames,
				"AND symboltype = 'Block' AND country.name = '" + countryname + "'");
		
		for (String[] s : flagBlockData)
		{
			flag.addBlock(
					Double.parseDouble(s[dbColumnLabels.get("xpos")]),
					Double.parseDouble(s[dbColumnLabels.get("ypos")]),
					Double.parseDouble(s[dbColumnLabels.get("size")]),
					Double.parseDouble(s[dbColumnLabels.get("whratio")]),
					getColour(s[dbColumnLabels.get("colour")]));
		}
	}
	
	private void getAllBlocks(TriangleFlag flag, String countryname)
	{
		List<String[]> flagBlockData = new ArrayList<String[]>();
		String[] tablenames = {"flagsymbol", "flag", "country"};
		flagBlockData = crud.selectAll(tablenames, 
				"AND symboltype IN ('Block', 'TriangleUp', 'TriangleSide') AND country.name = '" + countryname + "' ORDER BY flagsymbol.view_order");
		
		for (String[] s : flagBlockData)
		{
			switch (s[2])
			{
			case "Block":
				flag.addBlock(
						Double.parseDouble(s[5]), 	//horizontal position
						Double.parseDouble(s[6]),	//vertical position
						Double.parseDouble(s[4]),	//width, in database as size
						Double.parseDouble(s[7]),	//width/height ratio, in database as orientation
						getColour(s[3]));			//colour
				break;
			case "TriangleUp":
				flag.addTriangle(
						Double.parseDouble(s[5]), 	//horizontal position
						Double.parseDouble(s[6]),	//vertical position
						Double.parseDouble(s[4]),	//base, in database as size
						Double.parseDouble(s[7]),	//base/height ratio, in database as orientation
						getColour(s[3]));			//colour
				break;
			case "TriangleSide":
				flag.addSideTriangle(
						Double.parseDouble(s[5]), 	//horizontal position
						Double.parseDouble(s[6]),	//vertical position
						Double.parseDouble(s[4]),	//base, in database as size
						Double.parseDouble(s[7]),	//base/height ratio, in database as orientation
						getColour(s[3]));			//colour
				break;
			}
		}
	}
	
	static Color getColour(String colourcode)
	{
		if (colourcode == null) return null;
		switch (colourcode)
		{
		case "RED":
			return Color.RED;
		case "BLUE":
			return Color.BLUE;
		case "BLACK":
			return Color.BLACK;
		case "GREEN":
			return Color.GREEN;
		case "YELLOW": 
			return Color.YELLOW;
		case "WHITE": 
			return Color.WHITE;
		case "TURQUOISE":
			return Color.TURQUOISE;
		case "ORANGE":
			return Color.ORANGE;
		default: 
			try 
			{
				return Color.web(colourcode);
			}
			catch(IllegalArgumentException iae)
			{
				return null; //This is caught later. Null can be black or white, depending on what object it is applied to
			}
		}
	}
	
	private List<Color> getColourList(String countryname)
	{
		List<String> list = crud.selectMulti(
				new String[] {"flagcolours", "flag", "country"}, 
				"colour_code",
				"AND colour_order > 0"
				+ " AND country.name = '" + countryname
				+ "' ORDER BY flagcolours.colour_order");
		List<Color> colourlist = new ArrayList<Color>();
		for (String s : list)
		{
			colourlist.add(getColour(s));
		}
		return colourlist;
	}
	
	private Flag getFlag(String countryname)
	{
		//Main function to build a flag. Returns a completed flag that can be displayed
		//First it checks for type. Then it sets up a query depending on what type it is
		//This function will return null if no flag can be made. The rest of the program expects possible null returns
		if (countryname == null || countryname.equals("") || crud == null) return null;
		
		String flagtype;
		Color background = Color.WHITE;
		String[] basicFlagData = crud.selectOne(new String[] {"flag", "country"}, 
				new String[] {"flagstyle", "background"}, 
				"AND country.name = '" + countryname + "'");
		
		switch (basicFlagData.length)
		{
		case 0: return null;
		default: background = getColour(basicFlagData[1]);
		case 1: flagtype = basicFlagData[0];
		} //that's right. No breaks needed. We want to execute both statements by default
		
		Flag flag;
		switch (flagtype)
		{
		case "block":
			flag = new BlockFlag(background);
			getAllBlocks((BlockFlag)flag, countryname);
			break;
		case "triangle":
			flag = new TriangleFlag(background);
			getAllBlocks((TriangleFlag)flag, countryname);
			break;
		case "diagonal":
			double rowOrientation;
			try 
			{
				rowOrientation = Double.parseDouble(crud.selectOne(
						new String[] {"flagcolours", "flag", "country"}, 
						"colour_code", 
						"AND country.name = '" + countryname + "' " +
						"AND colour_order = 0"));
			}
			catch (NumberFormatException nfe)
			{
				rowOrientation = 1;
			}
			flag = new DiagonalFlag(background, getColourList(countryname));
			((DiagonalFlag)flag).setRowSizes(getRowSizes(countryname), true);
			((DiagonalFlag)flag).setDiagonalOrientation(rowOrientation);
			break;
		case "horizontal":
			flag = new HorizontalFlag(getColourList(countryname));
			break;
		case "vertical":
			flag = new VerticalFlag(getColourList(countryname));
			break;
		case "simple": default:
			flag = new Flag(background);
			break;
		}
		getAllFlagSymbols(flag, countryname);
		return flag;
	}
	
	public Flag getFlagByCountryName(String countryname)
	{
		return getFlag(countryname);
	}
	
	public Flag getRandomFlag()
	{
		int random = (int)(Math.random() * flags.size());
		return getFlag(flags.get(random));
	}
	
	private List<Double> getRowSizes(String countryname)
	{
		List<String> list = crud.selectMulti(
				new String[] {"flagcolours", "flag", "country"}, 
				"row_width",
				"AND colour_order > 0"
				+ " AND country.name = '" + countryname
				+ "' ORDER BY flagcolours.colour_order");
		List<Double> rowSizes = new ArrayList<>();
		for (String s : list)
		{
			rowSizes.add(Double.parseDouble(s));
		}
		return rowSizes;
	}
	
	private SymbolType getSymbol(String s)
	{
		switch (s) 
		{
		case "Circle": default:
			return SymbolType.CIRCLE;
		case "Star":
			return SymbolType.STAR;
		case "Crescent":
			return SymbolType.CRESCENT;
		}
	}
	
	private void insertAllSymbols(Flag flag, int key)
	{
		crud.insertMultiInOrder("flagsymbol (flag_id, symboltype, orientation, xPosition, yPosition, size, colour, view_order)", 
					flag.getSymbolData(), key);
	}
	
	private void insertAllBlocks(BlockFlag flag, int key)
	{
		crud.insertMultiInOrder(
				"flagsymbol (flag_id, symboltype, size, orientation, xPosition, yPosition, colour, view_order)", 
				flag.getBlockData(), key);
	}
	
	private int insertCountry(String countryname, String flagtype, String background)
	{
		int countryid = crud.insertAndReturnKey("country (name)", new String[] {countryname});
		switch (flagtype)
		{
		case "horizontal": case "vertical:":
			return crud.insertAndReturnKey("flag (country_id, flagstyle)", new String[] {String.valueOf(countryid), flagtype});
		default:
			return crud.insertAndReturnKey("flag (country_id, flagstyle, background)", 
					new String[] {String.valueOf(countryid), flagtype, background});
		}
	}
	
	public boolean saveFlagToDB(Flag flag, String countryname, boolean overwrite)
	{
		if (crud == null) return false;
		if (overwrite) throw new RuntimeException("Overwriting flags has been disabled"); //TODO is this going to remain disabled?
		int key = 0;
		if (flag instanceof BlockFlag)
		{
			key = insertCountry(countryname, flag instanceof TriangleFlag ? "triangle" : "block", flag.getBackGround().toString());
			insertAllBlocks((BlockFlag)flag, key);
		}
		else if (flag instanceof DiagonalFlag)
		{
			key = insertCountry(countryname, "diagonal", flag.getBackGround().toString());
			List<String[]> list = new ArrayList<>();
			Color colorlist[] = ((SimpleFlag)flag).getColors();
			double rows[] = ((DiagonalFlag)flag).getRowSizes();
			for (int i = 0; i < colorlist.length; i++)
			{
				list.add(new String[] {colorlist[i].toString(), String.valueOf(i + 1), String.valueOf(rows[i])});
			}
			crud.insertMulti("flagcolours (flag_id, colour_code, colour_order, row_width)", list, key);
			crud.insertWithKey("flagcolours (flag_id, colour_code, colour_order)",
					new String[] {String.valueOf(((DiagonalFlag)flag).getOrientation()), "0"}, key );
		}
		else if (flag instanceof SimpleFlag) 
		{
			key = insertCountry(countryname, flag instanceof HorizontalFlag ? "horizontal" : "vertical", null);
			List<String[]> list = new ArrayList<>();
			Color colorlist[] = ((SimpleFlag)flag).getColors();
			for (int i = 0; i < colorlist.length; i++)
			{
				list.add(new String[] {colorlist[i].toString(), String.valueOf(i + 1)});
			}
			crud.insertMulti("flagcolours (flag_id, colour_code, colour_order)", list, key);
		}
		else
		{
			key = insertCountry(countryname, "simple", flag.getBackGround().toString());					
		}
		insertAllSymbols(flag, key);
		flags.add(countryname);
		Logger.logToFile("Succesfully created flag of " + countryname + " at " + LocalDate.now().toString());
		return true;
	}
}
