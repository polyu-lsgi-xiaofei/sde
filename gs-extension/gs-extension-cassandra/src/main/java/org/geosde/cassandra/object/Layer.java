package org.geosde.cassandra.object;

import java.util.UUID;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "catalog", name = "layer", readConsistency = "QUORUM", writeConsistency = "QUORUM", caseSensitiveKeyspace = false, caseSensitiveTable = false)
public class Layer {
	@PartitionKey(0)
	private String workspace;
	@PartitionKey(1)
	private String category;
	@ClusteringColumn(0)
	private String layer_name;
	@ClusteringColumn(1)
	private long cdate;
	private String geometry_type;
	private String geometry_column;
	private String keywords;
	private double maxx;
	private double maxy;
	private double minx;
	private double miny;
	private String owner;
	private int srid;
	
	public Layer() {
		// TODO Auto-generated constructor stub
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLayer_name() {
		return layer_name;
	}

	public void setLayer_name(String layer_name) {
		this.layer_name = layer_name;
	}

	public long getCdate() {
		return cdate;
	}

	public void setCdate(long cdate) {
		this.cdate = cdate;
	}

	public String getGeometry_type() {
		return geometry_type;
	}

	public void setGeometry_type(String geometry_type) {
		this.geometry_type = geometry_type;
	}

	public String getGeometry_column() {
		return geometry_column;
	}

	public void setGeometry_column(String geometry_column) {
		this.geometry_column = geometry_column;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public double getMaxx() {
		return maxx;
	}

	public void setMaxx(double maxx) {
		this.maxx = maxx;
	}

	public double getMaxy() {
		return maxy;
	}

	public void setMaxy(double maxy) {
		this.maxy = maxy;
	}

	public double getMinx() {
		return minx;
	}

	public void setMinx(double minx) {
		this.minx = minx;
	}

	public double getMiny() {
		return miny;
	}

	public void setMiny(double miny) {
		this.miny = miny;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getSrid() {
		return srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}
	
	
	
}
