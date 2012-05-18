package husacct.graphics.presentation.figures;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.TextFigure;

public class ParentFigure extends BaseFigure {
	private static final long serialVersionUID = 5449552267500654293L;

	private RectangleFigure top;
	private RectangleFigure body;
	private TextFigure text;

	private static final int MIN_WIDTH = 500;
	private static final int MIN_HEIGHT = 500;

	public ParentFigure(String name) {
		super(name);

		top = new RectangleFigure();
		body = new RectangleFigure();
		text = new TextFigure(getName());

		children.add(top);
		children.add(body);
		children.add(text);

		top.set(AttributeKeys.FILL_COLOR, defaultBackgroundColor);
	}

	@Override
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
		// minimum size
		if ((lead.x - anchor.x) < MIN_WIDTH) {
			lead.x = anchor.x + MIN_WIDTH;
		}
		if ((lead.y - anchor.y) < MIN_HEIGHT) {
			lead.y = anchor.y + MIN_HEIGHT;
		}

		top.setBounds(anchor, new Point2D.Double(anchor.x + (lead.x - anchor.x) * 0.33f, anchor.y + (lead.y - anchor.y) * 0.2f));

		Point2D.Double bodyTopLeft = new Point2D.Double(anchor.x, (anchor.y + top.getBounds().height));

		body.setBounds(bodyTopLeft, lead);

		// textbox centralising
		double plusX = (((lead.x - bodyTopLeft.x) - text.getBounds().width) / 2);
		double plusY = (((lead.y - bodyTopLeft.y) - text.getBounds().height) / 2);

		Point2D.Double textAnchor = (Double) bodyTopLeft.clone();
		textAnchor.x += plusX;
		textAnchor.y += plusY;
		text.setBounds(textAnchor, null);

		invalidate();
	}

	@Override
	public ParentFigure clone() {
		ParentFigure other = (ParentFigure) super.clone();

		other.top = top.clone();
		other.body = body.clone();
		other.text = text.clone();

		other.children = new ArrayList<Figure>();
		other.children.add(other.top);
		other.children.add(other.body);
		other.children.add(other.text);

		return other;
	}
	
	public void addChildFigure(Figure figure){
		figure.transform(new AffineTransform(0, 0, 0, 0, 25, 37));
		add(figure);
	}
	
	public void changed(){
		super.changed();
		for (Figure child : children) {
			if(child instanceof BaseFigure)
				((BaseFigure)child).updateLocation(getBounds().getX(), getBounds().getY());
		}
	}
	
	@Override
	public boolean isModule() {
		return false;
	}

	@Override
	public boolean isLine() {
		return false;
	}
}
