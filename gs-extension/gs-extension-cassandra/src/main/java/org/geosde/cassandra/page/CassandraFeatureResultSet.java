package org.geosde.cassandra.page;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.geosde.cassandra.SessionRepository;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.TableMetadata;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class CassandraFeatureResultSet implements IResultSet<SimpleFeature> {

	public final static Session session = SessionRepository.getSession();
	public final static Map<DataType, Class> CA_MAP_TO_TYPE = new HashMap<DataType, Class>() {
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

	private List<SimpleFeature> features;
	private List<SimpleStatement> statements;
	private SimpleStatement currentStatement;
	private Envelope bbox = null;
	private SimpleFeatureType sft = null;
	private CassandraFeaturePage page;

	public CassandraFeatureResultSet(List<SimpleStatement> statements, Envelope bbox) {
		this.statements = statements;
		this.features = new ArrayList<>();
		this.sft = getSchema(new NameImpl("gis_osm_pois_free_1"),
				SessionRepository.getMetadata().getKeyspace("usa").getTable("gis_osm_pois_free_1"));
		this.bbox = bbox;
		session.execute("use usa;");
		currentStatement = statements.remove(0);
		page = new CassandraFeaturePage(session, currentStatement, sft, bbox);

	}

	public void all() {
		int count = 0;
		List<SimpleFeature> list = fetchMoreResult();
		while (list != null) {
			count += list.size();
			list = fetchMoreResult();
		}
		System.out.println(count);
	}

	public List<SimpleFeature> fetchMoreResult() {
		List<SimpleFeature> list = null;
		if (!page.isFullyFetched()) {
			list = page.nextPage();
			return list;
		} else if (statements.size() > 0) {
			currentStatement = statements.remove(0);
			page = new CassandraFeaturePage(session, currentStatement, sft, bbox);
			list = page.nextPage();
			return list;
		} else {
			return list;
		}
	}

	@Override
	public boolean hasNext() {
		if (!features.isEmpty()) {
			return true;
		} else {
			do {
				features = fetchMoreResult();
				if (features == null) {
					return false;
				}
			} while (features.isEmpty());
			return true;
		}
	}

	@Override
	public SimpleFeature next() {
		return features.remove(0);
	}

	private SimpleFeatureType getSchema(Name name, TableMetadata table) {
		List<ColumnMetadata> columns = table.getColumns();
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(name);
		AttributeTypeBuilder attrTypeBuilder = new AttributeTypeBuilder();
		for (ColumnMetadata cm : columns) {
			String cname = cm.getName();
			Class binding = CA_MAP_TO_TYPE.get(cm.getType());
			if (!cm.getName().equals("cell") && !cm.getName().equals("epoch") && !cm.getName().equals("pos")
					&& !cm.getName().equals("timestamp") && !cm.getName().equals("fid")) {
				if (Geometry.class.isAssignableFrom(binding)) {
					attrTypeBuilder.binding(binding);
					CoordinateReferenceSystem wsg84 = null;
					try {
						wsg84 = CRS.decode("EPSG:4326");
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

}
