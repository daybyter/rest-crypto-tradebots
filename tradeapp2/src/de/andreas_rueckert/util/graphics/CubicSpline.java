/**
 * Java implementation of b splines.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.util.graphics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Java implementation of a b spline generator.
 *
 * @see http://www.javaprogrammingforums.com/java-programming-tutorials/6121-java-tip-nov-20-2010-spline-interpolation.html
 */
public class CubicSpline {

    // Inner classes

    /**
     * A comparator for 2 points, that sorts the pixel on the x axes.
     */
    static final Comparator<Point> POINT_ORDER = 
	
	new Comparator<Point>() {
	
	/**
	 * Compare 2 points p1 and p2.
	 *
	 * @param p1 The first point.
	 * @param p2 The second point.
	 *
	 * @param p1.x - p2.x as the comparison value.
	 */
	public int compare( Point p1, Point p2) {
	    return ( p1.x - p2.x);
	}
    };


    // Static variables


    // Instance variables


    // Constructors


    // Methods

    /**
     * Compute the pixels of a b spline curve from an array of pixel points.
     *
     * @param knots The pixels, the computed curve must include.
     *
     * @return An array of points, representing a b spline curve.
     */
    public static Point [] getSplineCurve( Point [] knots) {

	if( knots.length > 2) {

	    // Create a buffer to prepare the input.
	    ArrayList<Point> input = new ArrayList<Point>();

	    // Add all the points to the array.
	    for( int i = 0; i < knots.length; ++i) {
		input.add( knots[ i]);
	    }
	    
	    // Just show the points for debugging purposes.
	    /* System.out.print( "The input points before sorting are: ");
	    for( int i = 0; i < input.size(); ++i) {
		System.out.print( "" + input.get(i).x + "," + input.get(i).y + " ");
	    }
	    System.out.println(""); */


	    // Start with sorting the points.
	    Collections.sort( input, POINT_ORDER);

	    // If 2 points share the same x coordinate, remove one of them.
	    for( int i = 0; i < input.size() - 1; ) {
		if( input.get( i).x == input.get( i + 1).x) {
		    input.remove( i + 1);  // Is this ok? Just ignoring the 2nd trade?
		} else {
		    ++i;
		}
	    }

	    // Just show the points for debugging purposes.
	    /* System.out.print( "The input points are: ");
	    for( int i = 0; i < input.size(); ++i) {
		System.out.print( "" + input.get(i).x + "," + input.get(i).y + " ");
	    }
	    System.out.println(""); */


	    // Are there enough points left to compute a spline?
	    if( input.size() > 2) {

		// Create a buffer for the result;
		ArrayList<Point> result = new ArrayList<Point>();

		// Split the knots in 4 point parts and interpolate the points in between them.
		double [] xCoordinates = new double[ 4];
		double [] yCoordinates = new double[ 4];
		
		for( int currentPoint = 0; currentPoint < ( input.size() - 1); ++currentPoint) {
		    
		    // If we are at the start, compute a dummy point before the curve.
		    if( currentPoint == 0) {
			
			// Create the dummy point.
			xCoordinates[ 0] = 2 * input.get( 0).getX() - input.get( 1).getX();
			yCoordinates[ 0] = 2 * input.get( 0).getY() - input.get( 1).getY();
		    } else {
			xCoordinates[ 0] = input.get( currentPoint - 1).getX();
			yCoordinates[ 0] = input.get( currentPoint - 1).getY();
		    }
		    
		    xCoordinates[ 1] = input.get( currentPoint).getX();
		    yCoordinates[ 1] = input.get( currentPoint).getY();
		    xCoordinates[ 2] = input.get( currentPoint + 1).getX();
		    yCoordinates[ 2] = input.get( currentPoint + 1).getY();

		    if( currentPoint == ( input.size() - 2)) {  // Is this the last point? Then create another dummy point behind the curve.
			xCoordinates[ 3] = 2 * input.get( currentPoint + 1).getX() - input.get( currentPoint).getX();
			yCoordinates[ 3] = 2 * input.get( currentPoint + 1).getY() - input.get( currentPoint).getY();
		    } else {
			xCoordinates[ 3] = input.get( currentPoint + 2).getX();
			yCoordinates[ 3] = input.get( currentPoint + 2).getY();
		    }

		    /*
		    System.out.print( "The 4 knots are: ");
		    for( int i = 0; i < 4; ++i) {
			System.out.print( "" + xCoordinates[ i] + "," + yCoordinates[i] + " ");
		    }
		    System.out.println("");
		    */
		    
		    // Now loop over the x coordinates between point 1 and 2.
		    for( int currentX = input.get( currentPoint).x; currentX < input.get( currentPoint + 1).x; ++currentX) {
			int currentY = (int)( Math.round( polyInterpolate( xCoordinates, yCoordinates, (double)currentX, 3)));
			
			result.add( new Point( currentX, currentY));
		    }
		}
		
		// Now convert the buffer to an array.
		return result.toArray( new Point[ result.size()]);
	    }
	}
	
	return knots;  // Return just the input as a default.
    }

    /**
     * Implementation of the linear equation solver according to Gauss.
     *
     * @param matrix The equation to solve as a 2d matrix.
     *
     * @return The solution as a 1d array.
     */
    private static double [] linearGaussSolver( double [] [] matrix) {

	double [] results = new double[ matrix.length];
	int [] order = new int[ matrix.length];

	for( int i = 0; i < order.length; ++i){
	    order[i] = i;
	}
	for( int i = 0; i < matrix.length; ++i)	{
	    
	    // partial pivot
	    int maxIndex = i;
	    
	    for( int j = i + 1; j < matrix.length; ++j)	{
		if( Math.abs( matrix[ maxIndex][ i]) < Math.abs( matrix[ j][ i])) {
		    maxIndex = j;
		}
	    }
	    if( maxIndex != i) {
		// swap order
		{
		    int temp = order[i];
		    order[i] = order[maxIndex];
		    order[maxIndex] = temp;
		}
		
		// swap matrix
		for( int j = 0; j < matrix[ 0].length; ++j) {
		    double temp = matrix[ i][ j];
		    matrix[ i][ j] = matrix[ maxIndex][ j];
		    matrix[ maxIndex][ j] = temp;
		}
	    }
	    
	    if( Math.abs( matrix[ i][ i]) < 1e-15) {
		throw new RuntimeException("Singularity detected");
	    }

	    for( int j = i + 1; j < matrix.length; ++j) {
		double factor = matrix[ j][ i] / matrix[ i][ i];
		for( int k = i; k < matrix[ 0].length; ++k) {
		    matrix[ j][ k] -= matrix[ i][ k] * factor;
		}
	    }
	}
	
	for( int i = matrix.length - 1; i >= 0; --i) {

	    // back substitute
	    results[ i] = matrix[ i][ matrix.length];

	    for( int j = i + 1; j < matrix.length; ++j) {
		results[ i] -= results[ j] * matrix[ i][ j];
	    }
	    results[ i] /= matrix[ i][ i];
	}
	
	double [] correctResults = new double[ results.length];

	for( int i = 0; i < order.length; ++i) {

	    // switch the order around back to the original order
	    correctResults[ order[ i]] = results[ i];
	}

	return results;
    }

    /**
     * Interpolate a point on a spline.
     *
     * @param dataX The array holding the x coordinates.
     * @param dataY The array holding the y coordinates.
     * @param x The x coordinate of the requested point.
     * @param power The power of the use function.
     *
     * @return The y coordinate of the requested point.
     */
    public static double polyInterpolate( double [] dataX, double [] dataY, double x, int power) {
	int xIndex = 0;
	
	while (xIndex < dataX.length - (1 + power + (dataX.length - 1) % power) && dataX[xIndex + power] < x) {
	    xIndex += power;
	}
 
	double matrix [] [] = new double[ power + 1][ power + 2];

	for( int i = 0; i < power + 1; ++i) {
	    for( int j = 0; j < power; ++j) {
		matrix[ i][ j] = Math.pow( dataX[ xIndex + i], ( power - j));
	    }
	    matrix[ i][ power] = 1;
	    matrix[ i][ power + 1] = dataY[ xIndex + i];
	}
	
	double [] coefficients = linearGaussSolver( matrix);
	double answer = 0;
	for( int i = 0; i < coefficients.length; ++i) {
	    answer += coefficients[ i] * Math.pow( x, ( power - i));
	}
	return answer;
    }
}