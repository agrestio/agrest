/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package io.agrest.backend.exp;

import io.agrest.backend.util.EqualsBuilder;

import java.util.Objects;

/**
 * Named parameter for parameterized expressions.
 */
public class ExpressionParameter {
	
	protected String name;

	/**
	 * Constructor for ExpressionParam.
	 */
	public ExpressionParameter(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the expression parameter.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return '$' + name;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ExpressionParameter)) {
			return false;
		}

		ExpressionParameter parameter = (ExpressionParameter) o;
		return nullSafeEquals(name, parameter.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	/**
	 * Compares two objects similar to "Object.equals(Object)". Unlike
	 * Object.equals(..), this method doesn't throw an exception if any of the
	 * two objects is null.
	 */
	protected boolean nullSafeEquals(Object o1, Object o2) {

		if (o1 == null) {
			return o2 == null;
		}

		// Arrays must be handled differently since equals() only does
		// an "==" for an array and ignores equivalence. If an array, use
		// the Jakarta Commons Language component EqualsBuilder to determine
		// the types contained in the array and do individual comparisons.
		if (o1.getClass().isArray()) {
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(o1, o2);
			return builder.isEquals();
		} else { // It is NOT an array, so use regular equals()
			return o1.equals(o2);
		}
	}
}
