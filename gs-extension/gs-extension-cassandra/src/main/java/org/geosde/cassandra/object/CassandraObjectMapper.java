package org.geosde.cassandra.object;

import org.geosde.cassandra.SessionRepository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

public class CassandraObjectMapper {

	public static void addtWorkspace(Workspace workspace) {
		Session session = SessionRepository.getSession();
		MappingManager manager = new MappingManager(session);
		Mapper<Workspace> mapper = manager.mapper(Workspace.class);
		mapper.save(workspace);
		session.close();
	}

	public static void addLayer(Layer layer) {
		Session session = SessionRepository.getSession();
		MappingManager manager = new MappingManager(session);
		Mapper<Layer> mapper = manager.mapper(Layer.class);
		mapper.save(layer);
		session.close();
	}

	public static Layer getLayer(String workspace, String category, String layername, long date) {
		Session session = SessionRepository.getSession();
		MappingManager manager = new MappingManager(session);
		Mapper<Layer> mapper = manager.mapper(Layer.class);
		return mapper.get(workspace,category,layername,date);

	}
	
	public static Layer getLayer(String workspace, String category, String layername) {
		Session session = SessionRepository.getSession();
		ResultSet rs=session.execute("SELECT cdate FROM catalog.layer;");
		long date=rs.one().getLong("cdate");
		MappingManager manager = new MappingManager(session);
		Mapper<Layer> mapper = manager.mapper(Layer.class);
		return mapper.get(workspace,category,layername,date);
	}
	public static void main(String[] args) {

	}
}
