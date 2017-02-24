package org.geosde.example;

import java.util.Map;

import org.geosde.cassandra.CassandraDataStore;
import org.geosde.cassandra.CassandraFeatureStore;
import org.geosde.compute.process.Process;
import org.geosde.compute.process.ProcessExecutor;
import org.geosde.compute.process.Processors;
import org.geosde.compute.process.Progress;
import org.geosde.compute.process.factory.DescribeParameter;
import org.geosde.compute.process.factory.DescribeProcess;
import org.geosde.compute.process.factory.DescribeResult;
import org.geosde.compute.process.factory.StaticMethodsProcessFactory;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.text.Text;
import org.geotools.util.KVP;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.OctagonalEnvelope;
import com.vividsolutions.jts.io.WKTReader;

public class ProcessTutorial extends StaticMethodsProcessFactory<ProcessTutorial> {

	public ProcessTutorial() {
		super(Text.text("Tutorial"), "tutorial", ProcessTutorial.class);
	}

	@DescribeProcess(title = "Octagonal Envelope", description = "Get the octagonal envelope of this Geometry.")
	@DescribeResult(description = "octagonal of geom")
	static public Geometry octagonalEnvelope(@DescribeParameter(name = "geom") Geometry geom) {
		return new OctagonalEnvelope(geom).toGeometry(geom.getFactory());
	}

	public void testProcess2() throws Exception {
		ProcessExecutor engine = Processors.newProcessExecutor(2);
		Name name = new NameImpl("geo", "buffer");
		Process process = Processors.createProcess(name);
		
		Filter filter = CQL.toFilter("BBOX(the_geom,-125.04,32.90,-113.34,42.36)");
		CassandraDataStore datastore = new CassandraDataStore();
		datastore.setCatalog_name("usa");
		datastore.setNamespaceURI("usa");
		String[] types = datastore.getTypeNames();
		for (String type : types) {
			System.out.println(type);
		}
		CassandraFeatureStore featureSource = (CassandraFeatureStore) datastore
				.getFeatureSource("gis.osm_pois_free_1");
		Query query = new Query(featureSource.getSchema().getTypeName(), filter, new String[] {});
		// featureSource.getReader(query);

		SimpleFeatureCollection features = featureSource.getFeatures(query);
		SimpleFeatureIterator iterator = features.features();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				System.out.println(feature.getDefaultGeometry());
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				Map<String, Object> input = new KVP("geom", geometry);
				input.put("distance", 10);
				Progress working1 = engine.submit(process, input);
				Map<String, Object> result = working1.get(); // get is BLOCKING
				Geometry octo = (Geometry) result.get("result");
				System.out.println(octo);
			}
		} catch (Throwable t) {
			iterator.close();
		}

	}

	public void testProcess() throws Exception {
		ProcessExecutor engine = Processors.newProcessExecutor(2);
		WKTReader wktReader = new WKTReader(new GeometryFactory());
		Geometry geom = wktReader.read("MULTIPOINT (1 1, 5 4, 7 9, 5 5, 2 2)");
		Map<String, Object> input = new KVP("geom", geom);
		input.put("distance", 10);
		Name name = new NameImpl("geo", "buffer");
		Process process = Processors.createProcess(name);
		System.out.println(process);
		Progress working1 = engine.submit(process, input);
		Map<String, Object> result = working1.get(); // get is BLOCKING
		Geometry octo = (Geometry) result.get("result");
		System.out.println(octo);

		name = new NameImpl("tutorial", "octagonalEnvelope");
		process = Processors.createProcess(name);

		System.out.println(process);
		// quick map of inputs
		Progress working2 = engine.submit(process, input);

		// you could do other stuff whle working is doing its thing
		if (working2.isCancelled()) {
			return;
		}
		result = working2.get(); // get is BLOCKING
		octo = (Geometry) result.get("result");
		System.out.println(octo);
	}

	public static void main(String[] args) throws Exception {

		new ProcessTutorial().testProcess2();
	}
}
