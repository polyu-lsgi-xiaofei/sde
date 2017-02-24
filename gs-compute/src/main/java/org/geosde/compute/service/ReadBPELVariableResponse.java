/* 
 * Copyright 2012 Michael Pantazoglou
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geosde.compute.service;

import org.geosde.compute.p2p.P2PResponse;

/**
 * This class implements the response of the ReadBPELVariable p2p service.
 * 
 * @author Michael Pantazoglou
 *
 */
public class ReadBPELVariableResponse extends P2PResponse {
	
	private static final long serialVersionUID = 201205051819L;
	
	/**
	 * The variable value.
	 */
	private String variableValue;

	/**
	 * Constructor.
	 */
	public ReadBPELVariableResponse() {
		super();
	}

	public String getVariableValue() {
		return variableValue;
	}

	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}

}
