package flags;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

public class BlockFlag extends Flag
{
	protected List<Rectangle> flagBlocks = new ArrayList<Rectangle>();
	protected Rectangle activeRectangle;
	
	public BlockFlag()
	{
		super();
	}
	
	public BlockFlag(Color clr)
	{
		super(clr);
	}
	
	public void addBlock(double width, double height, Pos position, Color clr)
	{
		width = Double.min(width, this.getWidth());
		height = Double.min(height, this.getHeight());
		Rectangle r = new Rectangle(width, height, clr);
		switch (position)
		{
		case TOP_LEFT: default:
			break;
		case TOP_RIGHT:
			r.setCoords(this.getWidth() - width, 0);
			break;
		case BOTTOM_LEFT:
			r.setCoords(0, this.getHeight() - height);
			break;
		case BOTTOM_RIGHT:
			r.setCoords(this.getWidth() - width, this.getHeight() - height);
			break;
		case CENTER:
			r.setCoords((this.getWidth() - width) / 2, (this.getHeight() - height) / 2);
			break;
		}
		flagBlocks.add(r);
		activeRectangle = r;
	}
	
	public void addBlock(double xPos, double yPos, double size, double widthHeightRatio, Color clr)
	{
		double hSize = Double.min(size, this.getWidth());
		double vSize = hSize / widthHeightRatio;
		if (vSize > this.getHeight())
		{
			vSize = this.getHeight();
			hSize = vSize * widthHeightRatio;
		}
		Rectangle r = new Rectangle(hSize, vSize, clr);
		if (xPos + hSize > this.getWidth()) xPos = this.getWidth() - hSize;
		if (yPos + vSize > this.getHeight()) yPos = this.getHeight() - vSize;
		r.setCoords(xPos, yPos);
		activeRectangle = r;
		flagBlocks.add(r);
	}
	
	protected void addSymbol(FlagSymbol fls)
	{
		symbols.add(fls);
		fls.draw();
		activeSymbol = fls;
		activeRectangle = null;
	}
	
	public boolean checkForSymbol(double x, double y)
	{
		//first we check for symbols. If there are none, we check if there's a block we clicked on
		if (super.checkForSymbol(x, y))
		{
			return true;
		}		
		else
		{
			//Since rectangles can not be moved, we have to select the highest rectangle in the view-order
			//That means we can't stop iterating after we have found one.
			//If there is another rectangle on top of it, then that one needs to be highlighted
			Rectangle tmp = null;
			for (Rectangle r : flagBlocks)
			{			
				if (x > r.xPos && x < r.xPos + r.width && y > r.yPos && y < r.yPos + r.height)
				{
					tmp = r;
				}
			}
			if (tmp != null)
			{
				tmp.highlight();
				activeSymbol = null;
				return true;
			}
			else
			{
				activeRectangle = null;
			}
		}		
		return false;
	}
	
	public void draw()
	{
		super.draw();
		for(Rectangle r : flagBlocks)
		{
			r.draw();
		}
		for(FlagSymbol f : symbols)
		{
			f.draw();
		}
	}
	
	public List<String[]> getBlockData()
	{
		List<String[]> result = new ArrayList<String[]>();
		for (Rectangle r : flagBlocks)
		{
			result.add(r.getBlockData());
		}
		return result;
	}
	
	public String getSVGData()
	{
		StringBuilder SVGData = new StringBuilder();
		SVGData.append("<rect width=\"" + getWidth() + "\" height =\"" + getHeight() + 
				"\" style=\"fill:rgb(" + getRGB(backgroundColour) +
				");\"></rect>"); //background
		for (Rectangle r : flagBlocks)
		{
			SVGData.append(r.getSVGData());
		}
		for (FlagSymbol s : symbols)
		{
			SVGData.append(s.getSVGPath());
		}
		return SVGData.toString();
	}
	
	public void removeSymbol()
	{
		if (activeSymbol != null)	
		{
			symbols.remove(activeSymbol);
			lastChange = ChangeType.DELETE;
			removedSymbol = activeSymbol;
			activeSymbol = null;
		}
		else if (activeRectangle != null)
		{
			flagBlocks.remove(activeRectangle);
			activeRectangle = null;
		}
		this.draw();
	}
	
	public void setSymbolColour(Color clr)
	{
		if (activeSymbol != null) super.setSymbolColour(clr);
		else if (activeRectangle != null) {
			lastColour = activeRectangle.colour;
			activeRectangle.colour = clr;
			this.draw();
			lastChange = ChangeType.RECOLOUR_RECTANGLE;
		}
	}
	
	public void undoLastChange()
	{
		if (lastChange == ChangeType.RECOLOUR_RECTANGLE)
		{
			activeRectangle.colour = lastColour;
			this.draw();
			objectColorPicker.setValue(lastColour);
		}
		else
		{
			super.undoLastChange();
		}
	}
	
	protected class Rectangle
	{
		double width, height;
		double xPos = 0, yPos = 0;
		Color colour;
		
		Rectangle(double width, double height, Color clr)
		{
			this.width = width > 0 ? width : getWidth() / 2;
			this.height = height > 0 ? height : getHeight() / 2;
			this.colour = clr != null ? clr : Color.BLACK;
		}
		
		void setCoords(double x, double y)
		{
			this.xPos = x;
			this.yPos = y;
		}
		
		void highlight()
		{
			objectColorPicker.setValue(this.colour);
			activeRectangle = this;
		}
		
		void draw()
		{
			gc.setFill(colour);
			gc.fillRect(xPos, yPos, width, height);
		}
		
		String[] getBlockData()
		{
			return new String[] {
					"block",
					String.valueOf(width),
					String.valueOf(width / height),
					String.valueOf(xPos),
					String.valueOf(yPos),
					colour.toString()};
		}
		
		String getSVGData()
		{
			StringBuilder SVGData = new StringBuilder();
			SVGData.append("<rect x=\"" + xPos + "\" y=\"" + yPos + "\" width=\"" + width + "\" height=\"" + height + 
					"\" style=\"fill(" + getRGB(this.colour) + ");\"></rect>");
			return SVGData.toString();
		}
	}
}
