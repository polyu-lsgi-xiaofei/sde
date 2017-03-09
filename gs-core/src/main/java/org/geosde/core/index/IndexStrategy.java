package org.geosde.core.index;

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public interface IndexStrategy {

	public Map<String,Object> index(Geometry geom);

	public Map<String,Object> index(double x, double y);
	
	public PrimaryKey getPrimaryKey();
}
