package net.sourceforge.metrics.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * To comply with Eclipse 3.0 API this class is needed to initialize the default values for the metrics preferences. It is references in the plugin.xml.
 * 
 * @author Frank Sauer
 */
public class MetricsPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer# initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefStore = MetricsPlugin.getDefault().getPreferenceStore();
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
