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

/**
 * Represents a column in a primary key.
 * 
 * @author Xiao Fei
 *
 */
public class PrimaryKeyColumn {

	String name;

	Class<?> type;
	
	boolean isPartitonKey;

	public PrimaryKeyColumn(String name, boolean isPartitonKey,Class<?> type) {
		this.name = name;
		this.type = type;
		this.isPartitonKey=isPartitonKey;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}
	
	public boolean isPartitonKey(){
		return isPartitonKey;
	}

}
