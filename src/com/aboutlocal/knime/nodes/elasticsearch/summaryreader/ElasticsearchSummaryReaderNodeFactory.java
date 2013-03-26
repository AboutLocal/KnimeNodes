package com.aboutlocal.knime.nodes.elasticsearch.summaryreader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ElasticsearchUUIDReader" Node.
 * 
 *
 * @author Kilian Thiel
 */
public class ElasticsearchSummaryReaderNodeFactory 
        extends NodeFactory<ElasticsearchSummaryReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElasticsearchSummaryReaderNodeModel createNodeModel() {
        return new ElasticsearchSummaryReaderNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ElasticsearchSummaryReaderNodeModel> createNodeView(final int viewIndex,
            final ElasticsearchSummaryReaderNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ElasticsearchSummaryReaderNodeDialog();
    }

}

