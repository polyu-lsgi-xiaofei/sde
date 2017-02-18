package org.geosde.cassandra;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.geotools.geometry.jts.CircularString;
import org.geotools.geometry.jts.CompoundCurve;
import org.geotools.geometry.jts.CurvePolygon;
import org.geotools.geometry.jts.MultiCurve;
import org.geotools.geometry.jts.MultiSurface;

import com.datastax.driver.core.DataType;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class CassandraTypeConvertor {

	// geometry type to class map
	final static Map<String, Class> TYPE_TO_CLASS_MAP = new HashMap<String, Class>() {
		{
			put("GEOMETRY", Geometry.class);
			put("GEOGRAPHY", Geometry.class);
			put("POINT", Point.class);
			put("POINTM", Point.class);
			put("LINESTRING", LineString.class);
			put("LINESTRINGM", LineString.class);
			put("POLYGON", Polygon.class);
			put("POLYGONM", Polygon.class);
			put("MULTIPOINT", MultiPoint.class);
			put("MULTIPOINTM", MultiPoint.class);
			put("MULTILINESTRING", MultiLineString.class);
			put("MULTILINESTRINGM", MultiLineString.class);
			put("MULTIPOLYGON", MultiPolygon.class);
			put("MULTIPOLYGONM", MultiPolygon.class);
			put("GEOMETRYCOLLECTION", GeometryCollection.class);
			put("GEOMETRYCOLLECTIONM", GeometryCollection.class);
			put("COMPOUNDCURVE", CompoundCurve.class);
			put("MULTICURVE", MultiCurve.class);
			put("CURVEPOLYGON", CurvePolygon.class);
			put("CIRCULARSTRING", CircularString.class);
			put("MULTISURFACE", MultiSurface.class);
			put("BYTEA", byte[].class);
		}
	};

	public static Map<Class, DataType> TYPE_TO_CA_MAP = new HashMap<Class, DataType>() {
		{
			put(Integer.class, DataType.cint());
			put(String.class, DataType.text());
			put(java.lang.Long.class, DataType.bigint());
			put(java.lang.Float.class, DataType.cfloat());
			put(java.lang.Double.class, DataType.cdouble());
			put(Date.class, DataType.timestamp());
			put(UUID.class, DataType.uuid());
			put(com.vividsolutions.jts.geom.Geometry.class, DataType.blob());
			put(Point.class, DataType.blob());
			put(MultiPolygon.class, DataType.blob());
			put(MultiLineString.class, DataType.blob());
		}
	};

	public static Map<DataType, Class> CA_MAP_TO_TYPE = new HashMap<DataType, Class>() {
		{
			put(DataType.cint(), Integer.class);
			put(DataType.text(), String.class);
			put(DataType.bigint(), java.lang.Long.class);
			put(DataType.cfloat(), java.lang.Float.class);
			put(DataType.cdouble(), java.lang.Double.class);
			put(DataType.timestamp(), Date.class);
			put(DataType.uuid(), UUID.class);
			put(DataType.blob(), Geometry.class);
		}
	};
}
