package flags;

import javafx.geometry.Pos;
import javafx.scene.paint.Color;

public class TriangleFlag extends BlockFlag
{
	public TriangleFlag()
	{
		super();
	}
	
	public TriangleFlag(Color clr)
	{
		super(clr);
	}
	
	public void addTriangle(double base, double height, Pos position, Color clr)
	{
		Rectangle t;
		switch(position)
		{
		case TOP_LEFT: default:
			t = new SideTriangle(base, height, clr);
			t.setCoords(0, (getHeight() - base) / 2);
			break;
		case BOTTOM_LEFT: case BOTTOM_RIGHT:
			t = new Triangle(base, height, clr);
			t.setCoords(getHeight() - height, (getWidth() - base) / 2);
			break;
		case TOP_RIGHT:
			t = new Triangle(base, -height, clr);
			t.setCoords(getWidth(), (getHeight() - base) / 2);
		case CENTER:
			t = new Triangle(base, height, clr);
			t.setCoords((getHeight() - height) / 2, (getWidth() - base) / 2);
			break;
		}
		flagBlocks.add(t);
	}
	
	public void addSideTriangle(double xPos, double yPos, double base, double baseHeightRatio, Color clr)
	{
		double vSize = Double.min(base, getHeight());
		double hSize = vSize / baseHeightRatio;
		if (hSize > getWidth())
		{
			hSize = getWidth();
			vSize = hSize * baseHeightRatio;
		}
		SideTriangle t = new SideTriangle(vSize, hSize, clr);
		t.setCoords(xPos, yPos);
		flagBlocks.add(t);
	}
	
	public void addTriangle(double xPos, double yPos, double base, double heightBaseRatio, Color clr)
	{
		double hSize = Double.min(base, getWidth());
		double vSize = hSize / heightBaseRatio;
		if (vSize > getHeight())
		{
			vSize = getHeight();
			hSize = vSize * heightBaseRatio;
		}
		Triangle t = new Triangle(hSize, vSize, clr);
		t.setCoords(xPos, yPos);
		flagBlocks.add(t);
	}
	
	protected class Triangle extends Rectangle
	{
		Triangle(double base, double height, Color clr)
		{
			super(base, height, clr);
		}
		
		String[] getBlockData()
		{
			return new String[] {
					"TriangleUp",
					String.valueOf(width),
					String.valueOf(xPos),
					String.valueOf(yPos),
					String.valueOf(width / height),
					colour.toString()};
		}
		
		void draw()
		{
			gc.setFill(colour);
			gc.fillPolygon(new double[] {xPos + width / 2, xPos, xPos + width}, new double[] {yPos, yPos + height, yPos + height}, 3);
		}
		
		String getSVGData()
		{
			StringBuilder SVGData = new StringBuilder();
			SVGData.append("<polygon ");
			SVGData.append("points=\"" + (xPos + width / 2) + "," + yPos + " " + xPos + "," + (yPos + height) + " " + (xPos + width) + "," + (yPos + height));
			SVGData.append("\" style=\"fill:rgb(" + getRGB(this.colour) + ");\"></polygon>");
			return SVGData.toString();
		}
	}
	
	protected class SideTriangle extends Rectangle
	{
		SideTriangle(double base, double height, Color clr)
		{
			super(base, height, clr);
		}
		
		String[] getBlockData()
		{
			return new String[] {
					"TriangleSide",
					String.valueOf(width),
					String.valueOf(width / height),
					String.valueOf(xPos),
					String.valueOf(yPos),
					colour.toString()};
		}
		
		void draw()
		{
			gc.setFill(colour);
			gc.fillPolygon(new double[] {xPos, xPos + height, xPos}, new double[] {yPos, yPos + width / 2, yPos + width}, 3);
		}
	}
}
