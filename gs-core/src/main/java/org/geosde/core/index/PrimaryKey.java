/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geosde.core.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary key of a table.
 *
 * @author Xiao Fei
 *
 */
public class PrimaryKey {
	/**
	 * The columns making up the primary key.
	 */
	List<PrimaryKeyColumn> columns;
	List<PrimaryKeyColumn> partiton_keys;

	/**
	 * Table name
	 */
	String tableName;

	public PrimaryKey(String tableName, List<PrimaryKeyColumn> columns) {
		this.tableName = tableName;
		this.columns = columns;
		partiton_keys=new ArrayList<>();
		for(PrimaryKeyColumn key:columns){
			if(key.isPartitonKey){
				partiton_keys.add(key);
			}
		}
	}

	public List<PrimaryKeyColumn> getColumns() {
		return columns;
	}

	public String getTableName() {
		return tableName;
	}

	public PrimaryKeyColumn getColumn(String name) {
		for (PrimaryKeyColumn col : columns) {
			if (name.equals((col.getName()))) {
				return col;
			}
		}
		return null;
	}

	public List<PrimaryKeyColumn> getPartitionKeys() {
		return partiton_keys;
	}

}
