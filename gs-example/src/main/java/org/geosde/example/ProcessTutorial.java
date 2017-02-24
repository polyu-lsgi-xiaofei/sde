package org.geosde.example;

import java.util.Map;

import org.geosde.compute.process.ProcessExecutor;
import org.geosde.compute.process.Processors;
import org.geosde.compute.process.Progress;
import org.geosde.compute.process.Process;
import org.geosde.compute.process.factory.DescribeParameter;
import org.geosde.compute.process.factory.DescribeProcess;
import org.geosde.compute.process.factory.DescribeResult;
import org.geosde.compute.process.factory.StaticMethodsProcessFactory;
import org.geotools.feature.NameImpl;
import org.geotools.text.Text;
import org.geotools.util.KVP;
import org.opengis.feature.type.Name;

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

	public static void main(String[] args)throws Exception {
		
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
}
