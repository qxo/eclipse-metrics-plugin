package net.sourceforge.metrics.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * To comply with Eclipse 3.0 API this class is needed to initialize the default
 * values for the metrics preferneces. It is references in the plugin.xml.
 * @author Frank Sauer
 */
public class MetricsPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences prefStore = MetricsPlugin.getDefault().getPluginPreferences();
		prefStore.setDefault("METRICS.decimals", Constants.FRACTION_DIGITS);
		prefStore.setDefault("METRICS.xmlformat", "net.sourceforge.metrics.internal.xml.MetricsFirstExporter");
		prefStore.setDefault("METRICS.enablewarnings", false);
		prefStore.setDefault("METRICS.defaultColor", "0,0,0");
		prefStore.setDefault("METRICS.linkedColor", "0,0,255");
		prefStore.setDefault("METRICS.outOfRangeColor", "255,0,0");
		prefStore.setDefault("METRICS.depGR_background", "1,17,68");
		prefStore.setDefault("METRICS.showProject", true);
		prefStore.addPropertyChangeListener(MetricsPlugin.getDefault());
	}

}
