/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2017 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.util;

import tech.mcprison.prison.internal.World;

/**
 * Represents the area between two {@link Location}s.
 *
 * @author Faizaan A. Datoo
 * @since API 1.0
 */
public class Bounds {

    private final Location min, max, center;
    
    private final int xBlockMin, xBlockMax, yBlockMin, yBlockMax, zBlockMin, zBlockMax; 

    private final double xMin, xMax, yMin, yMax, zMin, zMax;
    
    private final int totalBlockCount;

    public Bounds(Location min, Location max) {
        this.min = min;
        this.max = max;
        
        this.xBlockMax = Math.max(min.getBlockX(), max.getBlockX());
        this.xBlockMin = Math.min(min.getBlockX(), max.getBlockX());
        
        this.yBlockMax = Math.max(min.getBlockY(), max.getBlockY());
        this.yBlockMin = Math.min(min.getBlockY(), max.getBlockY());
        
        this.zBlockMax = Math.max(min.getBlockZ(), max.getBlockZ());
        this.zBlockMin = Math.min(min.getBlockZ(), max.getBlockZ());
        
        this.xMin = Math.min(min.getX(), max.getX());
        this.xMax = Math.max(min.getX(), max.getX());
        
        this.yMin = Math.min(min.getY(), max.getY());
        this.yMax = Math.max(min.getY(), max.getY());
        
        this.zMin = Math.min(min.getZ(), max.getZ());
        this.zMax = Math.max(min.getZ(), max.getZ());
        
        double centerX = (xBlockMin + xBlockMax) / 2.0d;
        double centerY = (yBlockMin + yBlockMax) / 2.0d;
        double centerZ = (zBlockMin + zBlockMax) / 2.0d;
        
        this.center = new Location(this.min.getWorld(), centerX, centerY, centerZ );

        this.totalBlockCount = 
        			(getyBlockMax() - getyBlockMin() + 1) *
        			(getxBlockMax() - getxBlockMin() + 1) *
        			(getzBlockMax() - getzBlockMin() + 1);
    }
    
    public void setWorld( World world ) {
    	if ( world != null ) {
    		
    		if ( getMin().getWorld() == null ) {
    			getMin().setWorld( world );
    		}
    		if ( getMax().getWorld() == null ) {
    			getMax().setWorld( world );
    		}
    		if ( getCenter().getWorld() == null ) {
    			getCenter().setWorld( world );
    		}
    	}
    }
    
    /**
     * Returns the width of the area.
     * This will always be positive.
     *
     * @return A double.
     */
    public double getWidth() {
        return (getxMax() - getxMin()) + 1;
    }

    /**
     * Returns the height of the area.
     * This will always be positive.
     *
     * @return A double.
     */
    public double getHeight() {
        return (getyMax() - getyMin()) + 1;
    }

    /**
     * Returns the length of the area.
     * This will always be positive.
     *
     * @return A double.
     */
    public double getLength() {
        return (getzMax() - getzMin()) + 1;
    }

    /**
     * Returns the surface area, which is represented by the formula <code>A=2(wl+hl+hw)</code>.
     *
     * @return A double.
     */
    // A=2(wl+hl+hw)
    public double getArea() {
        return 2 * (getWidth() * getLength() + getHeight() * getLength()
            + getHeight() * getWidth());
    }

    public boolean within(Location location) {
    	return within( location, false );
    }
    
    public boolean withinIncludeTopOfMine(Location location) {
    	return within( location, true );
    }
    
    /**
     * Returns whether or not a single point is within these boundaries.  Ensure the same worlds
     * are being compared too.
     * 
     * Found an issue where if there is a one block gap under the mine, then technically the player
     * is not standing within the mine, but their head is within it.  So there is a risk they will 
     * suffocate upon a reset.  Therefore, we must subtract one from the mine's min y axis.
     *
     * @param location The {@link Location} to check.
     * @return true if the location is within the bounds, false otherwise.
     */
    private boolean within(Location location, boolean includeTopOfMine ) {
    	boolean results = false;
    	
    	if ( withinSameWorld( location )) {

    		double ourX = Math.floor(location.getX());
    		double ourY = Math.floor(location.getY());
    		double ourZ = Math.floor(location.getZ());
    		
    		results = ourX >= getxMin() && ourX <= getxMax() // Within X
    				&& ourY >= (getyMin() - 1) && ourY <= (getyMax() + (includeTopOfMine ? 1 : 0)) // Within Y
    				&& ourZ >= getzMin() && ourZ <= getzMax(); // Within Z
    	}

        return results;
    }
    
    public boolean withinSameWorld(Location location) {
    	return getCenter().getWorld() != null && location.getWorld() != null &&
    			getCenter().getWorld().getName().equalsIgnoreCase( 
    						location.getWorld().getName() );
    	}	
    
    /**
     * <p>This function will determine if the given location is within a radius of the Bounds' 
     * center. This will return a true or false value.
     * </p>
     * 
     * <p>To keep the calculations simple, if the worlds are the same, then it will create a new
     * Bounds object and then utilize the computed values for width and length by feeding it in 
     * to a Pythagorean theorem to compute the hypotenuse (distance) between these two points.
     * There maybe other unused values calculated, but going for the simplicity of reusing existing code.
     * </p>
     * 
     * @param location
     * @param radius
     * @return
     */
    public boolean within(Location location, long radius) {
    	boolean results = false;
    	
    	if ( withinSameWorld( location ) ) {
    		
    		// Ignore y since this is radius from the center axis of the mine:
    		double distance = getDistance(location);
    		
    		results = distance <= radius;
    	}
    	
    	return results;
    }

    /**
     * <p>Ignore Y since this is the radius from the center axis of the mine.
     * </p>
     * @return
     */
    public double getDistance() {
    	double deltaX = getMin().getX() - getMax().getX();
    	double deltaZ = getMin().getZ() - getMax().getZ();
    	double distance = Math.sqrt( (deltaX * deltaX)  + (deltaZ * deltaZ) );
    	return Math.round( distance );
    }
  
    public double getDistance(Location location) {
    	double deltaX = getCenter().getX() - location.getX();
    	double deltaZ = getCenter().getZ() - location.getZ();
    	double distance = Math.sqrt( (deltaX * deltaX)  + (deltaZ * deltaZ) );
		return Math.round( distance );
    }    	
    
    public double getDistance3d(Location location) {
    	double deltaX = getCenter().getX() - location.getX();
    	double deltaY = getCenter().getY() - location.getY();
    	double deltaZ = getCenter().getZ() - location.getZ();
    	double distance = Math.sqrt( (deltaX * deltaX) + (deltaY * deltaY)  + (deltaZ * deltaZ) );
    	return distance;
    }    	

    public String getDimensions() {
    	return "&7" + Math.round(getWidth()) + "&8x&7" +
                Math.round(getHeight()) + "&8x&7" + Math.round(getLength());
    }
    
    public Location getMin() {
        return min;
    }

    public Location getMax() {
        return max;
    }

    public Location getCenter()
	{
		return center;
	}

	@Override public String toString() {
        return "Bounds{" + "min=" + min.toCoordinates() + ", max=" + max.toCoordinates() + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bounds)) {
            return false;
        }

        Bounds bounds = (Bounds) o;
        return min != null ?
            min.equals(bounds.min) :
            bounds.min == null && (max != null ? max.equals(bounds.max) : bounds.max == null);
    }

    @Override public int hashCode() {
        int result = min != null ? min.hashCode() : 0;
        result = 31 * result + (max != null ? max.hashCode() : 0);
        return result;
    }

	public int getxBlockMin()
	{
		return xBlockMin;
	}

	public int getxBlockMax()
	{
		return xBlockMax;
	}
	
	public int getyBlockMin()
	{
		return yBlockMin;
	}

	public int getyBlockMax()
	{
		return yBlockMax;
	}

	public int getzBlockMin()
	{
		return zBlockMin;
	}

	public int getzBlockMax()
	{
		return zBlockMax;
	}

	public double getxMin()
	{
		return xMin;
	}

	public double getxMax()
	{
		return xMax;
	}

	public double getyMin()
	{
		return yMin;
	}

	public double getyMax()
	{
		return yMax;
	}

	public double getzMin()
	{
		return zMin;
	}

	public double getzMax()
	{
		return zMax;
	}

	public int getTotalBlockCount()
	{
		return totalBlockCount;
	}

}
