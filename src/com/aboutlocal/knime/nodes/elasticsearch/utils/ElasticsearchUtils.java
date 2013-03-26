/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.utils;

import java.util.Set;
import java.util.TreeSet;

import org.elasticsearch.action.admin.indices.stats.IndicesStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;

import com.aboutlocal.hypercube.domain.summary.elasticsearch.SummaryIndexHelper;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> © 2012 about:Local GmbH
 *
 */
public final class ElasticsearchUtils {

	private static ElasticsearchUtils instance = null;
	
	private final SummaryIndexHelper sih;
	
	private ElasticsearchUtils() { 
		sih = new SummaryIndexHelper();
	}
	
	public static final synchronized ElasticsearchUtils getInstance() {
		if (instance == null) {
			instance = new ElasticsearchUtils();
		}
		return instance;
	}
	
	public SummaryIndexHelper getSummaryIndexHelper() {
		return sih;
	}
	
	public String getDefaultSummaryIndex() {
		return sih.getSummaryIndex();
	}
	
	public Set<String> getSummaryIndices() {
		Set<String> indices = new TreeSet<>();
		try {
			IndicesStatsRequest isr = new IndicesStatsRequest();
			isr.all();
			IndicesStats istats = sih.getClient().admin().indices().stats(isr)
					.actionGet();

			for (String index : istats.getIndices().keySet()) {
				if (index.startsWith("summary")) {
					indices.add(index);
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR: Could not read elasticsearch indices!");
			e.printStackTrace();
			throw e;
		}
        return indices;
	}
}
