/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.summaryupdater;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> © 2013 about:Local GmbH
 *
 */
public final class ElasticsearchSummaryUpdaterConfigKeys {

	public static final String INDEX = "Index";
	
	public static final String FIELDNAME = "FieldName";
	
	public static final String FIELDTYPE = "FieldType";
	
	public static final String VALUECOL = "ValueColumn";
	
	public static final String ESIDCOL = "ElasticsearchIdColumn";
	
	public static final String CHUNKSIZE = "ChunkSize";
	
	public static final String MAXTHREADS = "MaxThreads";
	
	private ElasticsearchSummaryUpdaterConfigKeys() { }
}
