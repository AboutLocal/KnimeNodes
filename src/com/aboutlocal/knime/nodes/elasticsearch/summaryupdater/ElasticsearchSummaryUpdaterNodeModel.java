/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.summaryupdater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.ThreadPool;

import com.aboutlocal.hypercube.domain.summary.data.ElasticsearchSummary;
import com.aboutlocal.hypercube.domain.summary.elasticsearch.SummaryIndexHelper;
import com.aboutlocal.hypercube.io.es.ElasticSearchUtils;
import com.aboutlocal.knime.nodes.elasticsearch.utils.ElasticsearchUtils;
import com.sun.media.sound.InvalidDataException;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> © 2013 about:Local GmbH
 *
 */
public class ElasticsearchSummaryUpdaterNodeModel extends NodeModel {

	public static final String DEF_FIELDNAME = "prediction";
	
	public static final String DEF_FIELDTYPE = ElasticsearchType.String.toString();
	
	public static final String DEF_VALUECOL = "";
	
	public static final String DEF_ESIDCOL = "uuid";
	
	public static final int DEF_MAX_THREADS = 4;
	
	public static final int MIN_MAX_THREADS = 1;
	
	public static final int MAX_MAX_THREADS = 1000;
	
	public static final int DEF_CHUNKSIZE = 5000;
	
	public static final int MAX_CHUNKSIZE = Integer.MAX_VALUE;
			
	public static final int MIN_CHUNKSIZE = 100;
	
	
	private SummaryIndexHelper sih = ElasticsearchUtils.getInstance().getSummaryIndexHelper();
	
	private SettingsModelString indexModel = ElasticsearchSummaryUpdaterNodeDialog.createIndexModel();
	
	private SettingsModelString fieldNameModel = ElasticsearchSummaryUpdaterNodeDialog.createFieldNameModel();
	
	private SettingsModelString fieldTypeModel = ElasticsearchSummaryUpdaterNodeDialog.createFieldTypeModel();
	
	private SettingsModelString valueColModel = ElasticsearchSummaryUpdaterNodeDialog.createValueColModel();
	
	private SettingsModelString esIdColModel = ElasticsearchSummaryUpdaterNodeDialog.createEsIdColModel();
	
	private SettingsModelIntegerBounded chunkSizeModel = ElasticsearchSummaryUpdaterNodeDialog.createChunksizeModel();
	
	private SettingsModelIntegerBounded maxThreadsModel = ElasticsearchSummaryUpdaterNodeDialog.createMaxThreadsModel();
	
    /**
     * Constructor for the node model.
     */
    protected ElasticsearchSummaryUpdaterNodeModel() {
        super(1, 0);
    }
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	String fieldValueCol = valueColModel.getStringValue();
    	int fieldValueColIndx = -1;
    	fieldValueColIndx = inSpecs[0].findColumnIndex(fieldValueCol);
    	
    	// check if value col exists
    	if (fieldValueColIndx < 0) {
    		throw new InvalidSettingsException("No column \"" + fieldValueCol + "\" found.");
    	}
    	
    	// check if values are compatible with strings
    	if (!inSpecs[0].getColumnSpec(fieldValueColIndx).getType().isCompatible(StringValue.class)) {
    		throw new InvalidSettingsException("Values of column \"" + fieldValueCol 
    				+ "\" are not compatible with strings.");
    	}
    	
    	// check uuid column
    	String uuidColName = esIdColModel.getStringValue();
    	int uuidColIndx = -1;
    	uuidColIndx = inSpecs[0].findColumnIndex(uuidColName);
    	if (uuidColIndx < 0) {
    		throw new InvalidSettingsException("No column \"" + uuidColName + "\" found.");
    	}    	
    	
        return new DataTableSpec[]{};
    }
    
    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.BufferedDataTable[], org.knime.core.node.ExecutionContext)
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
    		final ExecutionContext exec) throws Exception {
    	
    	sih.setSummaryIndex(indexModel.getStringValue());
    	String newFieldName = fieldNameModel.getStringValue();
    	String newFieldType = fieldTypeModel.getStringValue();
    	String fieldValueCol = valueColModel.getStringValue();
    	String rootType = ElasticSearchUtils.rootType(new ElasticsearchSummary());
    	
    	int maxThreads = maxThreadsModel.getIntValue();
    	int chunkSize = chunkSizeModel.getIntValue();
    	
    	Client esClient = sih.getClient();
    	exec.checkCanceled();
    	
    	// CHECK MAPPING
    	ClusterState cs = esClient.admin().cluster().prepareState()
    			.setFilterIndices(sih.getSummaryIndex()).execute().actionGet().getState();
    	IndexMetaData imd = cs.getMetaData().index(sih.getSummaryIndex());
    	MappingMetaData mmd = imd.mapping(rootType);
    	
    	if (mmd != null) {
    		// mapping specifications
    		XContentBuilder xbMapping = XContentFactory.jsonBuilder();
    		xbMapping = xbMapping.startObject().startObject(rootType).startObject("properties");
    		xbMapping = xbMapping.startObject(newFieldName);
    		xbMapping = xbMapping.field("type", newFieldType);
    		xbMapping = xbMapping.field("index", "not_analyzed");
    		xbMapping.endObject().endObject().endObject().endObject();
    		
    		PutMappingResponse response = esClient.admin().indices().preparePutMapping(sih.getSummaryIndex())
    				.setIgnoreConflicts(true)
    				.setType(rootType)
    				.setSource(xbMapping)
    				.execute().actionGet();
    		if (!response.acknowledged()) {
    			this.setWarningMessage("Mapping " + newFieldName + "[" + newFieldType + "] could not be added!");
    			throw new NoSuchFieldException("Mapping " + newFieldName + "[" + newFieldType 
    					+ "] could not be added!");
    		}
    	} else {
			throw new NoSuchFieldException("No mapping found for type " + rootType);
    	}
    	
    	// update selected field with values must be specified (as well as chunk size)
    	BufferedDataTable summaries = inData[0];
    	
    	// check uuid column
    	String uuidColName = esIdColModel.getStringValue();
    	int uuidColIndx = -1;
    	uuidColIndx = summaries.getDataTableSpec().findColumnIndex(uuidColName);
    	if (uuidColIndx < 0) {
    		throw new InvalidDataException("No column with name " + uuidColName 
    				+ " containing elasticsearch summary ids!");
    	}
    	
    	// check values column
    	int fieldValueColIndx = -1;
    	fieldValueColIndx = summaries.getDataTableSpec().findColumnIndex(fieldValueCol);
    	if (fieldValueColIndx < 0) {
    		throw new InvalidDataException("No column with name " + fieldValueCol 
    				+ " containing values to update!");    		
    	}
    	
    	final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool();
    	final Semaphore semaphore = new Semaphore(maxThreads);
    	
    	int count = 0;
    	int maxCount = summaries.getRowCount();
    	List<DataRow> chunk = null;
    	AtomicInteger currentCount = new AtomicInteger();
    	for (DataRow row : summaries) {
    		count++;
    		exec.checkCanceled();
    		
    		if (chunk == null) {
    			chunk = new ArrayList<DataRow>(chunkSize);
    		}
    		chunk.add(row);
    		
    		if (chunk.size() >= chunkSize) {
    			pool.submit(new ElasticsearchUpdater(semaphore, chunk, sih.getSummaryIndex(), uuidColIndx, 
    					fieldValueColIndx, newFieldType, newFieldName, exec, currentCount, maxCount));
    			chunk = null;
    		}
    	}

		if (chunk != null && chunk.size() > 0) {
			pool.submit(new ElasticsearchUpdater(semaphore, chunk, sih.getSummaryIndex(), uuidColIndx, 
					fieldValueColIndx, newFieldType, newFieldName, exec, currentCount, maxCount));
			chunk = null;
		}
		
		pool.waitForTermination();
    	
    	return new BufferedDataTable[]{};
    }
    
    class ElasticsearchUpdater implements Runnable {

    	private String index;
    	
    	private List<DataRow> rows;
    	
    	private Semaphore semaphore;
    	
    	private int uuidColIndx;
    	
    	private int fieldValueColIndx;
    	
    	private String newFieldType;
    	
    	private String rootType = ElasticSearchUtils.rootType(new ElasticsearchSummary());
    	
    	private String newFieldName;
    	
    	private ExecutionContext exec;
    	
    	final AtomicInteger progressCounter;
    	
    	final int totalRowCount;
    	
    	public ElasticsearchUpdater(final Semaphore sem, final List<DataRow> rows, final String index, 
    			final int uuidColIndex, final int fieldValColIndex, final String fieldType, final String fieldName,
    			final ExecutionContext exec, final AtomicInteger progressCounter, final int totalRowCount) {
    		this.semaphore = sem;
    		this.rows = rows;
    		
    		this.index = index;
    		this.uuidColIndx = uuidColIndex;
    		this.fieldValueColIndx = fieldValColIndex;
    		this.newFieldType = fieldType;
    		this.newFieldName = fieldName;
    		this.exec = exec;
    		this.progressCounter = progressCounter;
    		this.totalRowCount = totalRowCount;
    	}
    	
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			SummaryIndexHelper helper = null;
			
			try {
				exec.checkCanceled();
				semaphore.acquire();
				
				helper = new SummaryIndexHelper();
				helper.setSummaryIndex(index);
				Client esClient = helper.getClient();
				
				for (DataRow row : rows) {
					exec.checkCanceled();
					
		    		String uuid = ((StringValue)row.getCell(uuidColIndx)).getStringValue();
		    		String newValue = ((StringValue)row.getCell(fieldValueColIndx)).getStringValue();
		    		
		    		// quote if string value
		    		if (ElasticsearchType.valueOf(newFieldType).equals(ElasticsearchType.String)) {
		    			newValue = "\"" + newValue + "\"";
		    		}
		    		
		    		esClient.prepareUpdate(sih.getSummaryIndex(), rootType, uuid)
		    			.setScript("ctx._source." + newFieldName + "=" + newValue + "").execute().actionGet();
		    		
		    		int count = progressCounter.addAndGet(1);
                    exec.setProgress((double)count/(double)totalRowCount, 
                    		"Updating summary " + count + " of " + totalRowCount);
				}
			} catch (CanceledExecutionException e) {
				
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			} finally {
				semaphore.release();
			}
		}
    }
    
    
	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		indexModel.saveSettingsTo(settings);
		fieldNameModel.saveSettingsTo(settings);
		fieldTypeModel.saveSettingsTo(settings);
		valueColModel.saveSettingsTo(settings);
		esIdColModel.saveSettingsTo(settings);
		chunkSizeModel.saveSettingsTo(settings);
		maxThreadsModel.saveSettingsTo(settings);
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		indexModel.validateSettings(settings);
		fieldNameModel.validateSettings(settings);
		fieldTypeModel.validateSettings(settings);
		valueColModel.validateSettings(settings);
		esIdColModel.validateSettings(settings);
		chunkSizeModel.validateSettings(settings);
		maxThreadsModel.validateSettings(settings);
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		indexModel.loadSettingsFrom(settings);
		fieldNameModel.loadSettingsFrom(settings);
		fieldTypeModel.loadSettingsFrom(settings);
		valueColModel.loadSettingsFrom(settings);
		esIdColModel.loadSettingsFrom(settings);
		chunkSizeModel.loadSettingsFrom(settings);
		maxThreadsModel.loadSettingsFrom(settings);
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		// Nothing to do ...
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File, org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing to do ...
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File, org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing to do ...
	}
}
