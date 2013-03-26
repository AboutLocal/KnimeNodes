package com.aboutlocal.knime.nodes.elasticsearch.summaryreader;

import java.io.File;
import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.aboutlocal.hypercube.domain.summary.data.CsvOutputSummary;
import com.aboutlocal.hypercube.domain.summary.data.Summary;
import com.aboutlocal.hypercube.domain.summary.elasticsearch.SummaryIndexHelper;
import com.aboutlocal.hypercube.domain.transformers.SummaryToRawCsvTransformer;
import com.aboutlocal.knime.nodes.elasticsearch.utils.ElasticsearchUtils;
import com.aboutlocal.knime.nodes.elasticsearch.utils.SummaryUtils;

/**
 * This is the model implementation of ElasticsearchUUIDReader.
 * 
 *
 * @author Kilian Thiel
 */
public class ElasticsearchSummaryReaderNodeModel extends NodeModel {
    
	public static final int MAX_MAXRESULTS = Integer.MAX_VALUE;
	
	public static final int MIN_MAXRESULTS = -1;
	
	public static final int DEF_MAXRESULTS = 1000;

	public static final int MAX_CHUNKSIZE = 10000;
	
	public static final int MIN_CHUNKSIZE = 100;
	
	public static final int DEF_CHUNKSIZE = 1000;
	
	
	private SettingsModelString indexModel = ElasticsearchSummaryReaderNodeDialog.createIndexModel();
	
	private SettingsModelString queryModel = ElasticsearchSummaryReaderNodeDialog.createQueryModel();
	
	private SettingsModelIntegerBounded maxResultsModel = ElasticsearchSummaryReaderNodeDialog.createMaxResultsModel();
	
	private SettingsModelIntegerBounded chunkSizeModel = ElasticsearchSummaryReaderNodeDialog.createChunksizeModel();
	
	private SummaryIndexHelper sih = ElasticsearchUtils.getInstance().getSummaryIndexHelper();
	
    /**
     * Constructor for the node model.
     */
    protected ElasticsearchSummaryReaderNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{SummaryUtils.createSummarySpec()};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	sih.setSummaryIndex(indexModel.getStringValue());
    	int maxResultSize = maxResultsModel.getIntValue();
    	int chunkSize = chunkSizeModel.getIntValue();
    	if (chunkSize > maxResultSize) {
    		chunkSize = maxResultSize;
    	}
    	
    	QueryBuilder query;
    	String queryString = queryModel.getStringValue();
    	if (queryString.isEmpty() || queryString.toLowerCase().equals("match all")) {
    		query = QueryBuilders.matchAllQuery();
    	} else {
    		query = QueryBuilders.queryString(queryString);
    	}
    	
    	BufferedDataContainer bdc = exec.createDataContainer(SummaryUtils.createSummarySpec());
    	SummaryToRawCsvTransformer transformer = new SummaryToRawCsvTransformer();
    	
    	int rowIndex = 0;
    	boolean end = false;
		
		Client esClient = sih.getClient();
		SearchResponse scrollResp = esClient.prepareSearch(sih.getSummaryIndex())
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setQuery(query)
                .setSize(chunkSize).execute().actionGet();
		
		while (!end) {
		    scrollResp = esClient.prepareSearchScroll(scrollResp.getScrollId())
		    		.setScroll(new TimeValue(600000)).execute().actionGet();
		    
			long maxResults = scrollResp.getHits().getTotalHits();
			if (maxResultSize > maxResults) {
				maxResultSize = (int)maxResults;
			}		    
		    
		    for (SearchHit hit : scrollResp.getHits()) {
				exec.checkCanceled();
				exec.setProgress((double) rowIndex / (double) maxResultSize,
						"Retrieving search hit " + rowIndex + " of " + maxResultSize);		    	
		    	
				if (hit != null) {
					rowIndex++;

					Summary s = sih.searchHitToSummary(hit);
					CsvOutputSummary csvSummary = transformer.transform(s);
					DataRow row = SummaryUtils.createDataRow(rowIndex,
							csvSummary);

					bdc.addRowToTable(row);
				}
				
			    if (rowIndex >= maxResultSize) {
			    	end = true;
			        break;
			    }
		    }
		    
		    if (scrollResp.hits().hits().length == 0 || rowIndex >= maxResultSize) {
		    	end = true;
		        break;
		    }

		}
    	
    	bdc.close();
        return new BufferedDataTable[]{bdc.getTable()};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	indexModel.saveSettingsTo(settings);
    	maxResultsModel.saveSettingsTo(settings);
    	queryModel.saveSettingsTo(settings);
    	chunkSizeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	indexModel.loadSettingsFrom(settings);
    	queryModel.loadSettingsFrom(settings);
    	maxResultsModel.loadSettingsFrom(settings);
    	chunkSizeModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        indexModel.validateSettings(settings);
        queryModel.validateSettings(settings);
        maxResultsModel.validateSettings(settings);
        chunkSizeModel.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }
}

