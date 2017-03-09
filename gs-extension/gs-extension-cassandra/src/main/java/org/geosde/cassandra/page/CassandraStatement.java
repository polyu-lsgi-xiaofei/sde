package org.geosde.cassandra.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;

import com.datastax.driver.core.SimpleStatement;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2RegionCoverer;
import com.vividsolutions.jts.geom.Envelope;

public class CassandraStatement {
	
	private Envelope bbox = null;
	private List<SimpleStatement> statements;
	private List<String> quad_ids = null;
	
	public CassandraStatement() {
		// TODO Auto-generated constructor stub
	}
	public CassandraFeatureResultSet query(double lat0,double lat1,double lon0,double lon1) {
		
		// String polygon =
		// "32.90:-125.04,32.9:-113.34,42.36:-113.34,42.36:-125.04;";
		bbox = new ReferencedEnvelope(lon0, lon1, lat0, lat1, DefaultGeographicCRS.WGS84);
		String polygon = lat0 + ":" + lon0 + "," + lat0 + ":" + lon1 + "," + lat1 + ":" + lon1 + "," + lat1 + ":" + lon0
				+ ";";
		S2Polygon a = makePolygon(polygon);
		ArrayList<S2CellId> covering = new ArrayList<>();
		S2RegionCoverer coverer = new S2RegionCoverer();
		coverer.setMinLevel(10);
		coverer.setMaxLevel(10);
		coverer.getCovering(a, covering);
		quad_ids=new ArrayList<>();
		statements=new ArrayList<>();
		for (S2CellId id : covering) {
			quad_ids.add(id.toToken());
			statements.add(new SimpleStatement(
					"select cell,epoch,fid,the_geom from gis_osm_pois_free_1 where cell = ? and epoch=?;", id.toToken(),
					"201612"));
		}
		System.out.println(quad_ids.size());
		return new CassandraFeatureResultSet(statements,bbox);

	}
	
	private S2Polygon makePolygon(String str) {
		List<S2Loop> loops = Lists.newArrayList();

		for (String token : Splitter.on(';').omitEmptyStrings().split(str)) {
			S2Loop loop = makeLoop(token);
			loop.normalize();
			loops.add(loop);
		}

		return new S2Polygon(loops);
	}

	private S2Loop makeLoop(String str) {
		List<S2Point> vertices = Lists.newArrayList();
		parseVertices(str, vertices);
		return new S2Loop(vertices);
	}

	private void parseVertices(String str, List<S2Point> vertices) {
		if (str == null) {
			return;
		}
		for (String token : Splitter.on(',').split(str)) {
			int colon = token.indexOf(':');
			if (colon == -1) {
				throw new IllegalArgumentException("Illegal string:" + token + ". Should look like '35:20'");
			}
			double lat = Double.parseDouble(token.substring(0, colon));
			double lng = Double.parseDouble(token.substring(colon + 1));
			vertices.add(S2LatLng.fromDegrees(lat, lng).toPoint());
		}
	}
	
	public static void main(String[] args) {
		double lat0 = 32.00;
		double lat1 = 35.00;
		double lon0 = -123.00;
		double lon1 = -118.00;
		CassandraFeatureResultSet rs=new CassandraStatement().query(lat0, lat1, lon0, lon1);
		
		Set<String> set=new TreeSet<>();
		int count=0;
		while(rs.hasNext()){
			SimpleFeature feature=rs.next();
			set.add(feature.getID());
			count++;
		}
		System.out.println("Set="+set.size());
		System.out.println("RS="+count);
		
		/*
		int count=0;
		Set<String> set=new TreeSet<>();
		while(rs.hasNext())
		{
			SimpleFeature feature=rs.next();
			System.out.println(count);
			System.out.println(feature);
			count++;
			break;
		}
		System.out.println(count);
		System.out.println(set.size());
		*/
	}
}
