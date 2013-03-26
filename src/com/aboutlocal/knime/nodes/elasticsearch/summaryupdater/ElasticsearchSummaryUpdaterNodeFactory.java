/**
 * Kilian Thiel, about:local GmbH, Berlin
 */
package com.aboutlocal.knime.nodes.elasticsearch.summaryupdater;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Kilian Thiel <Kilian.Thiel@about-local.com> © 2013 about:Local GmbH
 *
 */
public class ElasticsearchSummaryUpdaterNodeFactory extends
		NodeFactory<ElasticsearchSummaryUpdaterNodeModel> {

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public ElasticsearchSummaryUpdaterNodeModel createNodeModel() {
		return new ElasticsearchSummaryUpdaterNodeModel();
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeView(int, org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<ElasticsearchSummaryUpdaterNodeModel> createNodeView(
			final int viewIndex, final ElasticsearchSummaryUpdaterNodeModel nodeModel) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new ElasticsearchSummaryUpdaterNodeDialog();
	}

}
