package flags;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import main_package.Flags;

public class Flag extends Canvas
{
	protected GraphicsContext gc;
	protected Color lastColour, backgroundColour = Color.WHITE;
	protected ColorPicker objectColorPicker;
	
	protected List<FlagSymbol> symbols = new ArrayList<FlagSymbol>();
	protected FlagSymbol activeSymbol, removedSymbol;
	protected enum ChangeType {ADD, DELETE, MOVE, RECOLOUR_SYMBOL, RECOLOUR_BACKGROUND, RECOLOUR_RECTANGLE, RESIZE, ROTATE, NONE}
	protected ChangeType lastChange = ChangeType.NONE;

	private static double defaultheight = 120;
	private static double defaultwidth = 200;
	protected double lastSize;
	protected boolean allowdragging = false;
	
	public Flag()
	{
		super(defaultwidth, defaultheight);
		gc = this.getGraphicsContext2D();
	}
	
	public Flag(Color colour)
	{
		this();
		this.backgroundColour = colour != null ? colour : Color.WHITE;
	}
	
	protected void addSymbol(FlagSymbol fsy)
	{
		symbols.add(fsy);
		fsy.draw();
		lastChange = ChangeType.ADD;
	}
	
	public void addCircle()
	{
		addSymbol(new FlagSymbol());
	}
	
	public void addSymbol(SymbolType ofType, Color clr, double size)
	{
		addSymbol(new FlagSymbol(size, clr, ofType));
	}
	
	public void addSymbol(SymbolType ofType, Color clr, double atX, double atY, double size, double orientation)
	{
		addSymbol(new FlagSymbol(size, clr, ofType, atX, atY, orientation));
	}
	
	public boolean checkForSymbol(double x, double y)
	{
		//checks if there is a flagsymbol at cooridnates x and y with pythagorean distance check
		//this means the clickable area can be larger than the actual area of the symbol, especially with crescents
		if (!Flags.getEditableSetting()) return false;
		for (FlagSymbol f : symbols)
		{
			if (Math.pow(x - f.xPos, 2) + Math.pow(y - f.yPos, 2) < Math.pow(f.size / 2, 2))
			{
				allowdragging = true;
				activeSymbol = f;
				f.highlight();
				return true;
			}
		}
		return false;
	}
	
	public void disallowdragging()
	{
		allowdragging = false;
	}
	
	public void draw()
	{
		gc.setFill(backgroundColour);
		gc.fillRect(0, 0, this.getWidth(), this.getHeight());
		for(FlagSymbol f : symbols)
		{
			f.draw();
		}
	}
	
	public Color getBackGround()
	{
		return this.backgroundColour;
	}
	
	protected String getRGB(Color clr)
	{
		return (int)(clr.getRed() * 255) + "," + (int)(clr.getGreen() * 255) + "," + (int)(clr.getBlue() * 255);
	}
	
	public String getSVGData()
	{
		StringBuilder SVGData = new StringBuilder();
		SVGData.append("<rect width=\"" + getWidth() + "\" height =\"" + getHeight() + 
				"\" style=\"fill:rgb(" + getRGB(backgroundColour) +
				");\"></rect>"); //background
		for (FlagSymbol s : symbols)
		{
			SVGData.append(s.getSVGPath());
		}
		return SVGData.toString();
	}
	
	public List<String[]> getSymbolData()
	{
		List<String[]> result = new ArrayList<String[]>();
		for (FlagSymbol s : symbols)
		{
			result.add(s.getSymbolData());
		}
		return result;
	}
	
	public boolean hasSymbol()
	{
		return symbols.size() > 0;
	}
	
	public void moveSymbolTo(double x, double y)
	{
		if (!allowdragging || activeSymbol == null) return;
		if (Flags.getSnapSetting())
		{
			snapToPosition(x, y);
		}
		else
		{
			activeSymbol.xPos = x;
			activeSymbol.yPos = y;
		}
		
		//puts a symbol back in the window if it's dragged outside of it
		if (activeSymbol.xPos < activeSymbol.size / 2)
		{
			activeSymbol.xPos = activeSymbol.size / 2;
		}
		else if (activeSymbol.xPos > this.getWidth() - activeSymbol.size / 2)
		{
			activeSymbol.xPos = this.getWidth() - activeSymbol.size / 2;
		}
		
		if (activeSymbol.yPos < activeSymbol.size / 2) 
		{ 
			activeSymbol.yPos = activeSymbol.size / 2;
		}			
		else if (activeSymbol.yPos > this.getHeight() - activeSymbol.size / 2) 
		{
			activeSymbol.yPos = this.getHeight() - activeSymbol.size / 2;
		}
		this.draw();
		lastChange = ChangeType.MOVE;
	}
	
	public void removeSymbol()
	{
		removedSymbol = activeSymbol;
		symbols.remove(activeSymbol);
		activeSymbol = null;
		lastChange = ChangeType.DELETE;
		this.draw();
	}
	
	public void resizeFlag(double scaleFactor)
	{
		setWidth(getWidth() * scaleFactor);
		setHeight(getHeight() * scaleFactor);
		for (FlagSymbol s : symbols)
		{
			s.scaleTo(scaleFactor);
		}
	}
	
	public boolean resizeSymbol(double px)
	{
		if (activeSymbol == null) return false;
		if (activeSymbol.xPos - activeSymbol.size / 2 - px < 0 ||
				activeSymbol.yPos - activeSymbol.size / 2 - px < 0 ||
				activeSymbol.xPos + activeSymbol.size / 2 + px > this.getWidth() || 
				activeSymbol.yPos + activeSymbol.size / 2 + px > this.getHeight() ||
				activeSymbol.size <= -px) //These conditions prevent the user from scaling a symbol outside the boundaries
		{
			return false;
		}
		else
		{
			lastSize = activeSymbol.size;
			activeSymbol.size += px;
			lastChange = ChangeType.RESIZE;
			this.draw();
			return true;
		}
	}
	
	public void rotateSymbol()
	{
		if (activeSymbol == null || activeSymbol.type.equals(SymbolType.CIRCLE)) return;
		else
		{
			activeSymbol.orientation += Math.PI / 4; //Pi/4 is exactly 1/8th of a full rotation
		}
		if (activeSymbol.orientation >= 2 * Math.PI)
		{
			activeSymbol.orientation = 0;
		}
		lastChange = ChangeType.ROTATE;
		this.draw();
	}
	
	public void setBackGround(Color clr)
	{
		this.lastColour = this.backgroundColour;
		this.backgroundColour = clr;
		this.draw();
		this.lastChange = ChangeType.RECOLOUR_BACKGROUND;
	}
	
	public void setColorPicker(ColorPicker cp)
	{
		this.objectColorPicker = cp;
		cp.setOnAction(e->setSymbolColour(cp.getValue()));
	}
	
	public void setSymbolColour(Color clr)
	{
		if (activeSymbol == null) return;
		lastColour = activeSymbol.colour;
		activeSymbol.colour = clr;
		activeSymbol.draw();
		lastChange = ChangeType.RECOLOUR_SYMBOL;
	}
	
	protected void snapToPosition(double x, double y)
	{
		if (Math.abs(x - getWidth() / 2) < getWidth() / 20 && Math.abs(y - getHeight() / 2) < getHeight() / 20)
		{
			activeSymbol.xPos = getWidth() / 2;
			activeSymbol.yPos = getHeight() / 2;
		}
		else
		{
			activeSymbol.xPos = x;
			activeSymbol.yPos = y;
		}
	}
	
	public void undoLastChange()
	{
		switch (lastChange)
		{
		case ADD:
			activeSymbol = removedSymbol;
			break;
		case DELETE:
			addSymbol(removedSymbol);
			break;
		case MOVE:
			activeSymbol.xPos = activeSymbol.lastXPos;
			activeSymbol.yPos = activeSymbol.lastYPos;
			break;
		case RECOLOUR_BACKGROUND:
			this.backgroundColour = lastColour;
			break;
		case RECOLOUR_SYMBOL:
			activeSymbol.colour = lastColour;
			objectColorPicker.setValue(lastColour);
			break;
		case RESIZE:
			activeSymbol.size = lastSize;
			break;
		case ROTATE:
			activeSymbol.orientation -= Math.PI / 4;
			if (activeSymbol.orientation < 0)
			{
				activeSymbol.orientation += 2 * Math.PI;
			}
			break;
		default:
		}
		lastChange = ChangeType.NONE;
		this.draw();
	}
	
	protected class FlagSymbol
	{
		protected Color colour =  Color.BLACK;
		protected double xPos, yPos, size, orientation;
		protected double lastXPos, lastYPos;
		SymbolType type = SymbolType.CIRCLE;
		
		FlagSymbol()
		{
			this.xPos = gc.getCanvas().getWidth() / 2;
			this.yPos = gc.getCanvas().getHeight() / 2;
			this.size = gc.getCanvas().getHeight() / 2;
		}
		
		FlagSymbol(Color colour)
		{
			this();
			this.colour = colour != null ? colour : Color.BLACK;
		}
		
		FlagSymbol(double size, Color colour, SymbolType type)
		{
			this(colour);
			this.size = size <= 0 ? 5 : size;
			this.type = type;
		}
		
		FlagSymbol(double size, Color colour, SymbolType type, double x, double y, double orientation)
		{
			this(size, colour, type);
			this.orientation = orientation;
			this.xPos = x == 0 ? getWidth() / 2 : x; //Neither coordinate can be 0, so getting 0 means unknown coordinates...
			this.yPos = y == 0 ? getHeight() / 2 : y; //...so we replace them with the default value
		}
		
		protected void highlight()
		{
			if (objectColorPicker != null) objectColorPicker.setValue(this.colour);
			lastXPos = xPos;
			lastYPos = yPos;
		}
		
		public void scaleTo(double scaleFactor)
		{
			this.size *= scaleFactor;
			this.xPos *= scaleFactor;
			this.yPos *= scaleFactor;
		}
		
		public String[] getSymbolData()
		{
			return new String[] {
					this.type.toString(),
					String.valueOf(this.orientation),
					String.valueOf(this.xPos),
					String.valueOf(this.yPos),
					String.valueOf(this.size),
					this.colour.toString()
			};
		}
		
		protected void draw()
		{
			gc.setFill(colour);
			switch(type)
			{
			case CIRCLE:
				gc.fillOval(xPos - size / 2, yPos - size / 2, size, size);
				break;
			case STAR:
				double nodes[][] = new double[2][5];
				for (int i = 0; i < 5; i++)
				{
					nodes[0][i] = xPos + size / 2 * Math.sin(4 * Math.PI * i / 5 + orientation);
					nodes[1][i] = yPos - size / 2 * Math.cos(4 * Math.PI * i / 5 + orientation);
					//sin and cos formulas give all points on a circle. All nodes of a star lie on a circle at regular intervals
					//with a centre of xPos, yPos, the vector cos(a),sin(a) gives the direction of the node
					//this is then multiplied by size/2 to get the right point on the canvas
					//a full circle has an angle of 2*PI, but we take steps of 2 when drawing a star
					//that's why we multiply by 4*PI and divide the circle in 5 parts
					//normally, you use sin for the y-axis and cos for the x-axis, but then the star would point sideways with orientation 0
					//by swapping the cos for sin and the sin for -cos, the star points upwards, which is what you want on a flag
				}
				gc.fillPolygon(nodes[0], nodes[1], 5);
				break;
			case CRESCENT:
				double sharpness = 2;
				gc.moveTo(xPos + size / 2 * Math.sin(orientation), yPos + size / 2 * Math.cos(orientation));
		        gc.beginPath();
		        gc.arc(xPos, yPos, size / 2, size / 2, 270 + radToDeg(orientation), -180);		        
		        gc.arc(xPos + size / (2 * sharpness) * Math.cos(orientation),
		        		yPos - size / (2 * sharpness) * Math.sin(orientation),
		        		size * Math.hypot(1, sharpness) / 4, size * Math.hypot(1, sharpness) / 4,
		        		180 - radToDeg(Math.atan(sharpness)) + radToDeg(orientation), 
		        		2 * radToDeg(Math.atan(sharpness)));
		        //Crescent works by drawing a semi-circle, then closing the path with a smaller segment of a larger circle
		        //The sharpness variable can be adjusted to make the crescent bulkier or narrower
		        //As sharpness increases, the center of the larger circle needs to be closer to the center of the semi-circle
		        //the arc it needs to cut out also becomes bigger with increased sharpness. Shaprness 0 means a semi-circle
		        //keep in mind that arclength is always in degrees, while sin and cos use radians. That's we we use the radToDeg conversion.
		        //Sharpness is currently a fixed value, but it could be related to size, as bigger crescents tend to be sharper.
				gc.closePath();
				gc.fill();
				break;
			default:
			}
		}
		
		protected String getSVGPath()
		{
			switch(type)
			{
			case CIRCLE:
				return "<circle cx=\"" + xPos + "\" cy=\"" + yPos + "\" r=\"" + size / 2 + "\" fill=\"rgb(" + getRGB(this.colour) + ")\"></circle>";
			case STAR:
				StringBuilder nodes = new StringBuilder();
				for (int i = 0; i < 5; i++)
				{
					nodes.append(xPos + size / 2 * Math.sin(4 * Math.PI * i / 5 + orientation));
					nodes.append(",");
					nodes.append(yPos - size / 2 * Math.cos(4 * Math.PI * i / 5 + orientation));
					nodes.append(" ");
				}
				return "<polygon points=\"" + nodes + "\" style=\"fill:rgb(" + getRGB(this.colour) + ");\"></polygon>";
			default: return "";
			case CRESCENT:
				throw new RuntimeException("I haven't implemented this yet.......not enough energy for it"); //TODO: this
			}
		}
		
		private double radToDeg(double radians)
		{
			return 180 * radians / Math.PI;
		}
	}
}
