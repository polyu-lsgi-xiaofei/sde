package org.geosde.cassandra;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.geosde.cassandra.object.CassandraObjectMapper;
import org.geosde.cassandra.object.Layer;
import org.geosde.core.data.ContentDataStore;
import org.geosde.core.data.ContentEntry;
import org.geosde.core.data.ContentFeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.TableMetadata;
import com.vividsolutions.jts.geom.Geometry;

public class CassandraDataStore extends ContentDataStore {

	/**
	 * Boolean marker stating whether the feature type is to be considered read
	 * only
	 */
	public static final String CASSANDRA_READ_ONLY = "cassandra.readOnly";
	public SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");

	public CassandraDataStore() {
	}

	//
	// API Implementation
	//
	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		SimpleFeatureType schema = entry.getState(Transaction.AUTO_COMMIT).getFeatureType();
		if (schema == null) {
			// if the schema still haven't been computed, force its computation
			// so that we can decide if the feature type is read only
			schema = getSchema(entry.getName());
			entry.getState(Transaction.AUTO_COMMIT).setFeatureType(schema);
		}
		schema = entry.getState(Transaction.AUTO_COMMIT).getFeatureType();

		Object readOnlyMarker = schema.getUserData().get(CASSANDRA_READ_ONLY);
		if (Boolean.TRUE.equals(readOnlyMarker)) {
			return new CassandraFeatureSource(entry, Query.ALL);
		} else {
			return new CassandraFeatureStore(entry);
		}

	}

	@Override
	protected List<Name> createTypeNames() throws IOException {
		Session session = SessionRepository.getSession();
		List<Name> typeNames = new ArrayList<>();
		String namespace = getNamespaceURI();
		SimpleStatement statement = new SimpleStatement(
				"SELECT workspace,category,layer_name,cdate FROM catalog.layer;");
		ResultSet rs = session.execute(statement);
		for (Row row : rs) {
			String workspace_name = row.getString("workspace");
			String layer_name = row.getString("layer_name");
			long cdate = row.getLong("cdate");

			if (namespace.equals(workspace_name))
				typeNames.add(new NameImpl(workspace_name, layer_name + "_" + formatter.format(new Date(cdate))));
		}
		session.close();
		return typeNames;
	}

	public void createSchema(SimpleFeatureType featureType, Date date) throws IOException {

		String workspace_name = getNamespaceURI();
		String layer_name = featureType.getTypeName().replace(".", "_");
		Date cdate = date;
		String owner = "xiaofei";
		String geometry_type = featureType.getGeometryDescriptor().getType().getName().getLocalPart();
		String geometry_column = "the_geom";
		int srid = getSRID(featureType);
		double minx = 0;
		double miny = 0;
		double maxx = 0;
		double maxy = 0;
		String keywords = featureType.getGeometryDescriptor().getType().getName().getLocalPart();

		Layer layer = new Layer();
		layer.setWorkspace(workspace_name);
		layer.setCategory("basic");
		layer.setLayer_name(layer_name);
		layer.setCdate(cdate.getTime());
		layer.setGeometry_column(geometry_column);
		layer.setGeometry_type(geometry_type);
		layer.setSrid(srid);
		layer.setMaxx(maxx);
		layer.setMaxy(maxy);
		layer.setMinx(minx);
		layer.setMiny(miny);
		layer.setOwner(owner);
		layer.setKeywords(keywords);
		CassandraObjectMapper.addLayer(layer);

		Session session = SessionRepository.getSession();
		StringBuilder builder = new StringBuilder();

		builder.append("CREATE TABLE IF NOT EXISTS " + workspace_name + "."
				+ featureType.getName().toString().replace(".", "_") + "_" + formatter.format(cdate) + " (");
		builder.append("cell text,");
		builder.append("pos text,");
		builder.append("fid text,");
		List<AttributeDescriptor> attrDes = featureType.getAttributeDescriptors();
		List<String> col_items = new ArrayList<>();
		String cols="";
		for (AttributeDescriptor attr : attrDes) {
			String col_name = attr.getLocalName();
			cols+=col_name+",";
			Class type = attr.getType().getBinding();
			builder.append(col_name + " " + CassandraTypeConvertor.TYPE_TO_CA_MAP.get(type).getName().toString() + ",");
		}
		builder.append("PRIMARY KEY (cell,pos,fid)");
		builder.append(");");
		System.out.println(builder.toString());
		session.execute(builder.toString());
		
		builder = new StringBuilder();
		builder.append("CREATE MATERIALIZED VIEW IF NOT EXISTS "+workspace_name+"."+featureType.getName().toString().replace(".", "_") + "_" + formatter.format(cdate)+"_view AS ");
		builder.append("SELECT cell, pos, fid,"+cols.substring(0,cols.length()-1));
		builder.append(" FROM "+workspace_name+"."+featureType.getName().toString().replace(".", "_") + "_" + formatter.format(cdate));
		builder.append(" WHERE fid IS NOT NULL AND cell IS NOT NULL AND pos IS NOT NULL");
		builder.append(" PRIMARY KEY ( fid, cell, pos );");
		System.out.println(builder.toString());
		session.execute(builder.toString());
		session.close();
	}

	@Override
	public void createSchema(SimpleFeatureType featureType) throws IOException {
		createSchema(featureType, new Date());
	}

	@Override
	public SimpleFeatureType getSchema(Name name) throws IOException {
		Session session = SessionRepository.getSession();
		String workspace_name = getNamespaceURI();
		String category = "basic";
		String layername = name.getLocalPart();
		KeyspaceMetadata keyspaceMetadata = SessionRepository.getMetadata().getKeyspace(workspace_name);
		TableMetadata table = keyspaceMetadata.getTable(layername);
		session.close();
		List<ColumnMetadata> columns = table.getColumns();
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(name);
		AttributeTypeBuilder attrTypeBuilder = new AttributeTypeBuilder();
		for (ColumnMetadata cm : columns) {
			String cname = cm.getName();
			Class binding = CassandraTypeConvertor.CA_MAP_TO_TYPE.get(cm.getType());
			if (!cm.getName().equals("cell_id") && !cm.getName().equals("epoch") && !cm.getName().equals("pos")
					&& !cm.getName().equals("timestamp") && !cm.getName().equals("fid")) {
				if (Geometry.class.isAssignableFrom(binding)) {
					attrTypeBuilder.binding(binding);
					CoordinateReferenceSystem wsg84 = null;
					try {
						wsg84 = DefaultGeographicCRS.WGS84;
					} catch (Exception e) {
						e.printStackTrace();
					}
					attrTypeBuilder.setCRS(wsg84);
					builder.add(attrTypeBuilder.buildDescriptor(cname, attrTypeBuilder.buildGeometryType()));
				} else {
					builder.add(attrTypeBuilder.binding(binding).nillable(false).buildDescriptor(cname));
				}
			}
		}
		return builder.buildFeatureType();

	}

	//
	// Internal methods
	//
	/**
	 * Looks up the geometry srs by trying a number of heuristics. Returns -1 if
	 * all attempts at guessing the srid failed.
	 */

	protected int getSRID(SimpleFeatureType featureType) {
		int srid = -1;
		CoordinateReferenceSystem flatCRS = CRS.getHorizontalCRS(featureType.getCoordinateReferenceSystem());
		try {
			Integer candidate = CRS.lookupEpsgCode(flatCRS, false);
			if (candidate != null)
				srid = candidate;
			else
				srid = 4326;
		} catch (Exception e) {
			// ok, we tried...
		}
		return srid;
	}

}
