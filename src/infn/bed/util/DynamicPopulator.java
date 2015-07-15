package infn.bed.util;

import infn.bed.geometry.GeometricConstants;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Dynamically populates a world.
 *
 * @author Angelo Licastro
 */
public class DynamicPopulator {
	
	/**
	 * Returns a dynamically populated world.
	 * 
	 * @param worldRectangle The world (a Rectangle2D.Double object) to dynamically populate.
	 * @return An ArrayList of Rectangle2D.Double objects that form a dynamically populated world.
	 * The position of each Rectangle2D.Double is relative to the position of worldRectangle.
	 */
	public static ArrayList<Rectangle2D.Double> getDynamicallyPopulatedWorld(Rectangle2D.Double worldRectangle) {
		if ((GeometricConstants.CHANNELS % GeometricConstants.ROWS) == 0) {
			ArrayList<Rectangle2D.Double> dynamicallyPopulatedWorldRectangle = new ArrayList<>(GeometricConstants.CHANNELS);
			double horizontalAdjustmentFactor = worldRectangle.getMinX();
			double verticalAdjustmentFactor = worldRectangle.getMinY();
			double itemWidth = worldRectangle.getWidth() / GeometricConstants.COLUMNS;
			double itemHeight = worldRectangle.getHeight() / GeometricConstants.ROWS;
			double x;
			double y;
			for (int i = 0; i < GeometricConstants.ROWS; i++) {
				y = verticalAdjustmentFactor + (worldRectangle.getHeight() / GeometricConstants.ROWS) * i;
				for (int j = 0; j < GeometricConstants.COLUMNS; j++) {
					x = horizontalAdjustmentFactor + (worldRectangle.getWidth() / GeometricConstants.COLUMNS) * j;
					dynamicallyPopulatedWorldRectangle.add(new Rectangle2D.Double(x, y, itemWidth, itemHeight));
				}
			}
			return dynamicallyPopulatedWorldRectangle;
		} else {
			throw new ArithmeticException();
		}
	}

}
