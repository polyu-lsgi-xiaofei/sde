package org.geosde.core.schema;

public class LayerSchema {

	private String layer_id;
	private String keyspace_name;
	private String table_name;
	private String owner;
	private String spatial_column;
	private double minx;
	private double miny;
	private double maxx;
	private double maxy;
	private long cdate;
	private int srid;
	private String spatial_reference;

	public String getLayer_id() {
		return layer_id;
	}

	public void setLayer_id(String layer_id) {
		this.layer_id = layer_id;
	}

	public String getKeyspace_name() {
		return keyspace_name;
	}

	public void setKeyspace_name(String keyspace_name) {
		this.keyspace_name = keyspace_name;
	}

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSpatial_column() {
		return spatial_column;
	}

	public void setSpatial_column(String spatial_column) {
		this.spatial_column = spatial_column;
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

	public long getCdate() {
		return cdate;
	}

	public void setCdate(long cdate) {
		this.cdate = cdate;
	}

	public int getSrid() {
		return srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public String getSpatial_reference() {
		return spatial_reference;
	}

	public void setSpatial_reference(String spatial_reference) {
		this.spatial_reference = spatial_reference;
	}

}
