package change;

import java.util.LinkedList;

import graphic.GraphicComponent;
import graphic.relations.LineView;

public class BufferCreation implements Changeable
{
	private boolean isCreated;
	private GraphicComponent gc;
	private LinkedList<LineView> associedLinesView;
	
	public BufferCreation(boolean isCreated, GraphicComponent gc)
	{
		this.isCreated = isCreated;
		this.gc = gc;
		associedLinesView = gc.getGraphicView().getLinesViewAssociedWith(gc);
	}

	@Override
	public void restore()
	{
		if (isCreated)
		{
			gc.restore();
			
			for (LineView lv : associedLinesView)
				
				lv.restore();
		}
		else
		{
			Change.setBlocked(true);
			gc.delete();
			Change.setBlocked(false);
		}
	}
}
