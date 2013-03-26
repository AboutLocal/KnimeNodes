/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package com.aboutlocal.knime;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.aboutlocal.hypercube.domain.summary.elasticsearch.SummaryIndexHelper;
import com.aboutlocal.hypercube.util.config.EnvironmentConfig;
import com.aboutlocal.knime.nodes.elasticsearch.utils.ElasticsearchUtils;

/**
 * This is the eclipse bundle activator.
 * Note: KNIME node developers probably won't have to do anything in here, 
 * as this class is only needed by the eclipse platform/plugin mechanism.
 * If you want to move/rename this file, make sure to change the plugin.xml
 * file in the project root directory accordingly.
 *
 * @author Kilian Thiel
 */
public class AboutLocalNodePlugin extends Plugin {
    // The shared instance.
    private static AboutLocalNodePlugin plugin;

    /**
     * The constructor.
     */
    public AboutLocalNodePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be started
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        // load About Local configs
        EnvironmentConfig.elastic.getMode();
    }

    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        
        // close elastic search connections
        SummaryIndexHelper sih = ElasticsearchUtils.getInstance().getSummaryIndexHelper();
        sih.close();
    }

    /**
     * Returns the shared instance.
     * 
     * @return Singleton instance of the Plugin
     */
    public static AboutLocalNodePlugin getDefault() {
        return plugin;
    }

}

