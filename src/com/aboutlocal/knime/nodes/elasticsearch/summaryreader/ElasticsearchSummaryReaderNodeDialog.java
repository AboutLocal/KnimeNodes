package com.aboutlocal.knime.nodes.elasticsearch.summaryreader;

import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.aboutlocal.knime.nodes.elasticsearch.utils.ElasticsearchUtils;

/**
 * <code>NodeDialog</code> for the "ElasticsearchUUIDReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kilian Thiel
 */
public class ElasticsearchSummaryReaderNodeDialog extends DefaultNodeSettingsPane {

	public static final SettingsModelString createIndexModel() {
		return new SettingsModelString(ElasticsearchSummaryReaderConfigKeys.INDEX, 
				ElasticsearchUtils.getInstance().getDefaultSummaryIndex());
	}
	
	public static final SettingsModelString createQueryModel() {
		return new SettingsModelString(ElasticsearchSummaryReaderConfigKeys.QUERY, 
				"match all");
	}
	
	public static final SettingsModelIntegerBounded createMaxResultsModel() {
		return new SettingsModelIntegerBounded(ElasticsearchSummaryReaderConfigKeys.MAX_RESULTS, 
				ElasticsearchSummaryReaderNodeModel.DEF_MAXRESULTS, ElasticsearchSummaryReaderNodeModel.MIN_MAXRESULTS,
				ElasticsearchSummaryReaderNodeModel.MAX_MAXRESULTS);
	}
	
	public static final SettingsModelIntegerBounded createChunksizeModel() {
		return new SettingsModelIntegerBounded(ElasticsearchSummaryReaderConfigKeys.CHUNKSIZE, 
				ElasticsearchSummaryReaderNodeModel.DEF_CHUNKSIZE, ElasticsearchSummaryReaderNodeModel.MIN_CHUNKSIZE,
				ElasticsearchSummaryReaderNodeModel.MAX_CHUNKSIZE);
	}	
	
    /**
     * New pane for configuring the ElasticsearchUUIDReader node.
     */
    protected ElasticsearchSummaryReaderNodeDialog() {
    	ElasticsearchUtils eutils = ElasticsearchUtils.getInstance();
    	
    	// INDEX SELECTION
    	Set<String> summaryIndices = eutils.getSummaryIndices();
    	DialogComponentStringSelection dcStringSelection = new DialogComponentStringSelection(createIndexModel(),
    			"Summary Index", summaryIndices);
    	addDialogComponent(dcStringSelection);
    	
    	// QUERY
    	DialogComponentMultiLineString dcQuery = new DialogComponentMultiLineString(createQueryModel(),
    			"Elasticsearch Query", false, 20, 6);
    	addDialogComponent(dcQuery);
    	
    	setHorizontalPlacement(true);
    	
    	// MAX RESULTS    	
    	DialogComponentNumber dcMaxResults = new DialogComponentNumber(createMaxResultsModel(), "Max. Results", 1000);
    	addDialogComponent(dcMaxResults);
    	
    	// CHUNK SIZE
    	DialogComponentNumber dcChunksize = new DialogComponentNumber(createChunksizeModel(), "Chunk Size", 1000);
    	addDialogComponent(dcChunksize);
    }
}


