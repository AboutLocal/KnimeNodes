/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;

import com.aboutlocal.hypercube.domain.annotations.KNIME;
import com.aboutlocal.hypercube.domain.summary.data.CsvOutputSummary;
import com.aboutlocal.hypercube.io.fs.annotations.CSV;
import com.aboutlocal.hypercube.util.reflection.ReflectionUtils;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> � 2013 about:Local GmbH
 *
 */
public final class SummaryUtils {

	public static DataTableSpec createSummarySpec() {
		List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
		
		CsvOutputSummary csvSummary = new CsvOutputSummary();
        for (Field f : ReflectionUtils.fieldLookup(csvSummary)) {
        	KNIME knimeAnno = f.getAnnotation(KNIME.class);
        	if (knimeAnno != null && knimeAnno.include()) {
        		String colName = nameOrAnnotation(f);
        		DataColumnSpecCreator dcsc = new DataColumnSpecCreator(colName, StringCell.TYPE);
        		colSpecs.add(dcsc.createSpec());
        	}
        }
        
        DataColumnSpec[] colSpecArr = colSpecs.toArray(new DataColumnSpec[]{});
        return new DataTableSpec(colSpecArr);
	}
	
    private static String nameOrAnnotation(final Field f) {
        KNIME a = f.getAnnotation(KNIME.class);
        if (a == null || a.name().isEmpty()) {
            return f.getName();
        }
        return a.name();
    }
    
    public static DataRow createDataRow(final int rowIndex, final CsvOutputSummary csvSummary) {
    	RowKey rk = RowKey.createRowKey(rowIndex);
    	
    	List<DataCell> cells = new ArrayList<DataCell>();
    	
    	cells.add(createStringCell(csvSummary.uuid));
    	cells.add(createStringCell(csvSummary.mgcUuid));
    	cells.add(createStringCell(csvSummary.mgUuid));
    	
    	cells.add(createStringCell(csvSummary.companyName));
    	cells.add(createStringCell(csvSummary.affiliateType));
    	cells.add(createStringCell(csvSummary.totalSummariesInParentCluster));
    
    	cells.add(createStringCell(csvSummary.primarySector));
    	cells.add(createStringCell(csvSummary.secondarySector));    	
    	
    	cells.add(createStringCell(csvSummary.street));
    	cells.add(createStringCell(csvSummary.numberBlock));
    	cells.add(createStringCell(csvSummary.zip));
    	cells.add(createStringCell(csvSummary.city));    	
    	
    	cells.add(createStringCell(csvSummary.phoneShifted1));
    	cells.add(createStringCell(csvSummary.phoneShifted2));
    	cells.add(createStringCell(csvSummary.imprintPhone));
    	
    	cells.add(createStringCell(csvSummary.faxShifted1));
    	cells.add(createStringCell(csvSummary.imprintFax));
    	
    	cells.add(createStringCell(csvSummary.owner1Name));
    	cells.add(createStringCell(csvSummary.owner2Name));
    	
    	cells.add(createStringCell(csvSummary.website1));
    	cells.add(createStringCell(csvSummary.website2));
    	cells.add(createStringCell(csvSummary.website3));
    	
    	cells.add(createStringCell(csvSummary.email1));
    	cells.add(createStringCell(csvSummary.email2));
    	
    	cells.add(createStringCell(csvSummary.allSources));
    	
    	cells.add(createStringCell(csvSummary.lastActivityTime));
    	cells.add(createStringCell(csvSummary.lastActivityDelta));
    	cells.add(createStringCell(csvSummary.lastActivitySource));
    	cells.add(createStringCell(csvSummary.numMentions));
    	
    	cells.add(createStringCell(csvSummary.activityIndex));
    	cells.add(createStringCell(csvSummary.activityClusterIndex));
    	
    	cells.add(createStringCell(csvSummary.premiumSources));
    	cells.add(createStringCell(csvSummary.premiumProfileUrls));
    	
    	cells.add(createStringCell(csvSummary.numKeywords));
    	cells.add(createStringCell(csvSummary.adwordsDescription));
    	
    	cells.add(createStringCell(csvSummary.numSold));
    	cells.add(createStringCell(csvSummary.dealSources));
    	cells.add(createStringCell(csvSummary.dealProfileLinks));
    	
    	cells.add(createStringCell(csvSummary.starsOverall));
    	cells.add(createStringCell(csvSummary.starsScaled));
    	
    	cells.add(createStringCell(csvSummary.numRatingsOverall));
    	cells.add(createStringCell(csvSummary.ratingSources));
    	
    	cells.add(createStringCell(csvSummary.numLikesOverall));
    	cells.add(createStringCell(csvSummary.numCheckinsOverall));
    	cells.add(createStringCell(csvSummary.socialSources));
    	cells.add(createStringCell(csvSummary.faceBookLink));
    	
    	cells.add(createStringCell(csvSummary.hasGeoCode));
    	cells.add(createStringCell(csvSummary.totalSrcEntities));
    	cells.add(createStringCell(csvSummary.totalSrcEntitiesWithNoAff));
    	cells.add(createStringCell(csvSummary.companyNameCount));
    	cells.add(createStringCell(csvSummary.websiteCount));
    	cells.add(createStringCell(csvSummary.emailCount));
    	
    	cells.add(createStringCell(csvSummary.sectorScore));
    	cells.add(createStringCell(csvSummary.sectorOverlapScore));
    	cells.add(createStringCell(csvSummary.sectorNameScore));
    	cells.add(createStringCell(csvSummary.addressScore));
    	cells.add(createStringCell(csvSummary.completenessScore));
    	cells.add(createStringCell(csvSummary.reachabilityScore));
    	cells.add(createStringCell(csvSummary.successfulWebsiteLookup));
    	cells.add(createStringCell(csvSummary.imprintDataExtracted));
    	cells.add(createStringCell(csvSummary.affDiversityScore));
    	cells.add(createStringCell(csvSummary.clusterDiversityScore));
    	cells.add(createStringCell(csvSummary.bestWebsiteState));
    	cells.add(createStringCell(csvSummary.minCriticalityLevel));
    	cells.add(createStringCell(csvSummary.totalSummaryScore));
    	
    	return new DefaultRow(rk, cells);
    }
    
    private static StringCell createStringCell(final String str) {
    	String s = str;
    	if (s == null) {
    		s = "";
    	}
    	return new StringCell(s);
    }

    private static StringCell createStringCell(final Date d) {
    	return new StringCell(dateToString(d));
    }
    
    private static StringCell createStringCell(final Integer i) {
    	return new StringCell(intToString(i));
    }
    
    private static StringCell createStringCell(final Double d) {
    	return new StringCell(doubleToString(d));
    }

    private static String dateToString(final Date d) {
    	if (d == null) {
    		return "";
    	}
    	return d.toString();
    }    
    
    private static String intToString(final Integer i) {
    	if (i == null) {
    		return "";
    	}
    	return i.toString();
    }
    
    private static String doubleToString(final Double d) {
    	if (d == null) {
    		return "";
    	}
    	return d.toString();
    }    
}

