/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.summaryupdater;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> © 2013 about:Local GmbH
 *
 */
public enum ElasticsearchType {
	String,
	Integer,
	Double;
	
	public static final Set<String> valuesAsStrings() {
		Set<String> valuesAsStrings = new HashSet<>();
		for (ElasticsearchType t : ElasticsearchType.values()) {
			valuesAsStrings.add(t.toString());
		}
		return valuesAsStrings;
	}
}
