package cyclops.visualization.drawing.map.painters;


import cyclops.visualization.drawing.painters.PainterData;
import cyclops.visualization.palette.PaletteColour;
import cyclops.visualization.palette.palettes.SingleColourPalette;
import cyclops.visualization.template.Rectangle;


/**
 * 
 * This class implements the drawing of a map using block pixel filling
 * 
 * @author Nathaniel Sherry, 2009
 */

public class FloodMapPainter extends MapPainter
{

	private PaletteColour c;
	
	public FloodMapPainter(PaletteColour c)
	{
		super(new SingleColourPalette(c));
		
		this.c = c;
		
	}


	@Override
	public void drawMap(PainterData p, float cellSize, float rawCellSize)
	{

		p.context.save();
	
			p.context.setSource(c);
			
			p.context.addShape(new Rectangle(0, 0, p.dr.dataWidth * cellSize, p.dr.dataHeight * cellSize));
			p.context.fill();

		p.context.restore();

	}

	
	@Override
	public void clearBuffer()
	{
	}


	@Override
	public boolean isBufferingPainter()
	{
		return false;
	}
	
}
