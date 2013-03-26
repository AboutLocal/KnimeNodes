/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.summaryupdater;

import java.util.Set;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.aboutlocal.knime.nodes.elasticsearch.utils.ElasticsearchUtils;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> © 2013 about:Local GmbH
 *
 */
public class ElasticsearchSummaryUpdaterNodeDialog extends
		DefaultNodeSettingsPane {

	public static final SettingsModelString createIndexModel() {
		return new SettingsModelString(ElasticsearchSummaryUpdaterConfigKeys.INDEX, 
				ElasticsearchUtils.getInstance().getDefaultSummaryIndex());
	}
	
	public static final SettingsModelString createFieldNameModel() {
		return new SettingsModelString(ElasticsearchSummaryUpdaterConfigKeys.FIELDNAME, 
				ElasticsearchSummaryUpdaterNodeModel.DEF_FIELDNAME);
	}
	
	public static final SettingsModelString createFieldTypeModel() {
		return new SettingsModelString(ElasticsearchSummaryUpdaterConfigKeys.FIELDTYPE, 
				ElasticsearchSummaryUpdaterNodeModel.DEF_FIELDTYPE);
	}
	
	public static final SettingsModelString createValueColModel() {
		return new SettingsModelString(ElasticsearchSummaryUpdaterConfigKeys.VALUECOL, 
				ElasticsearchSummaryUpdaterNodeModel.DEF_VALUECOL);
	}
	
	public static final SettingsModelString createEsIdColModel() {
		return new SettingsModelString(ElasticsearchSummaryUpdaterConfigKeys.ESIDCOL, 
				ElasticsearchSummaryUpdaterNodeModel.DEF_ESIDCOL);
	}		
	
	public ElasticsearchSummaryUpdaterNodeDialog() {
		ElasticsearchUtils eutils = ElasticsearchUtils.getInstance();
		
    	// INDEX SELECTION
    	Set<String> summaryIndices = eutils.getSummaryIndices();
    	DialogComponentStringSelection dcStringSelection = new DialogComponentStringSelection(createIndexModel(),
    			"Summary Index", summaryIndices);
    	addDialogComponent(dcStringSelection);
		
    	// FIELD SPEC
    	DialogComponentString dcFieldName = new DialogComponentString(createFieldNameModel(),
    			"Name of the elasticsearch field");
    	addDialogComponent(dcFieldName);
    	
    	Set<String> fieldTypes = ElasticsearchType.valuesAsStrings();
    	DialogComponentStringSelection dcFieldValue = new DialogComponentStringSelection(createFieldTypeModel(),
    			"Type of the elasticsearch field", fieldTypes);
    	addDialogComponent(dcFieldValue);
    	
    	@SuppressWarnings("unchecked")
		DialogComponentColumnNameSelection dcValueCol = new DialogComponentColumnNameSelection(createValueColModel(),
    			"Column containing values to update", 0, StringValue.class);
    	addDialogComponent(dcValueCol);
    	
    	@SuppressWarnings("unchecked")
		DialogComponentColumnNameSelection dcEsIdCol = new DialogComponentColumnNameSelection(createEsIdColModel(),
    			"Column containing elasticsearch ids", 0, StringValue.class);
    	addDialogComponent(dcEsIdCol);
	}
}
