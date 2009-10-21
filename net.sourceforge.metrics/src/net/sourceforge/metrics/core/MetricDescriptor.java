/*
 * Copyright (c) 2003 Frank Sauer. All rights reserved.
 *
 * Licenced under CPL 1.0 (Common Public License Version 1.0).
 * The licence is available at http://www.eclipse.org/legal/cpl-v10.html.
 *
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 *
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES  NO REPRESENTATIONS OR WARRANTIES,
 * EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL THE
 * AUTHOR  BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT LIMITATION, LOST REVENUE,  PROFITS
 * OR DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL  OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF  LIABILITY, ARISING OUT OF OR RELATED TO
 * ANY FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR
 * HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *
 * created on Jan 13, 2003
 */
package net.sourceforge.metrics.core;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sourceforge.metrics.propagators.AvgValue;
import net.sourceforge.metrics.propagators.MaxValue;
import net.sourceforge.metrics.propagators.Sum;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Contains the specification of a metric as provided by a plugin manifest.
 * 
 * @author Frank Sauer
 */
public class MetricDescriptor {

	private static String[] levels = new String[] {
		"method", "type", "compilationUnit", "packageFragment", "packageFragmentRoot", "project"
	};
	
	private String defaultHint;
	private Double defaultMax;
	private Double defaultMin;
	private String hint;
	private Double max;
	private Double min;	
	private String id = null;
	private String name = null;
	private String sumOf = null;
	private String level = null;
	private String newAvgMaxAt = null;
	private boolean propagateSum = true;
	private boolean propagateAvg = true;
	private boolean propagateMax = true;
	private boolean allowDisable = true;
	private String[] requires = null; 
	
	/**
	 * Constructor MetricDescriptor.
	 * @param id
	 * @param name
	 * @param propagate
	 */
	private MetricDescriptor(String id, String name, String level) {
		this.id = id;
		this.name = name;
		this.level = level;
	}

	/**
	 * create a list of propagators (AvgValue, MaxValue, Sum) applicable to
	 * this metric as specified in the manifest xml.
	 * @return List
	 */
	public List createPropagators() {
		List result = new ArrayList();
		String nextLevel = getParentLevel();
		if (nextLevel != null) {
			if (isPropagateAvg()) {
				AvgValue v = new AvgValue(getId(),getLevel());
				result.add(v);
			}
			if (isPropagateMax()) {
				MaxValue max = new MaxValue(getId(),getLevel());
				result.add(max);
			}
			if (isPropagateSum()) {
				Sum s = new Sum(getId(),getId());
				result.add(s);
			}
		}
		return result;
	}
	
	/**
	 * Create a new AvgValue based on the newAvgMaxAt attribute in the xml
	 * @return List
	 */
	public List createIntroducedAvgMax() {
		List result = new ArrayList();
		if (newAvgMaxAt != null) {
			String per = getPreviousLevel(newAvgMaxAt);
			result.add(new AvgValue(getId(),per));
			result.add(new MaxValue(getId(),per));
		}
		return result;
	}
	
	/**
	 * create a new MetricDescriptor from the &lt;metric&gt; element
	 * @param element
	 * @return MetricDescriptor
	 */
	public static MetricDescriptor createFrom(IConfigurationElement element) {
		String id = element.getAttribute("id");
		String name = element.getAttribute("name");
		String level = element.getAttribute("level");
		String sumOf = element.getAttribute("sumOf");
		String newAvgMaxAt = element.getAttribute("newAvgMaxAt");
		boolean doSum = getBooleanAttribute(element,"propagateSum",true);
		boolean doAvg = getBooleanAttribute(element,"propagateAvg",true);
		boolean doMax = getBooleanAttribute(element,"propagateMax",true);		
		String requires = element.getAttribute("requires");	
		if ((id != null) && (name != null) && (level != null)) {
			MetricDescriptor m =  new MetricDescriptor(id, name, level);
			m.propagateAvg = doAvg;
			m.propagateMax = doMax;
			m.propagateSum = doSum;
			m.sumOf = sumOf;
			m.newAvgMaxAt = newAvgMaxAt;
			m.allowDisable = getBooleanAttribute(element,"allowDisable",true);
			IConfigurationElement[] ranges = element.getChildren("range");
			if (ranges.length>0) {
				IConfigurationElement range = ranges[0];
				String minStr = range.getAttribute("min");
				String maxStr = range.getAttribute("max");
				String hint   = range.getAttribute("hint");
				m.setRange(minStr, maxStr, hint);
			}
			if (requires != null && requires.length()>0) {
				StringTokenizer t = new StringTokenizer(requires, ",");
				List result = new ArrayList();
				while (t.hasMoreTokens()) {
					result.add(t.nextToken().trim());
				} 
				m.requires = (String[]) result.toArray(new String[]{});
			} 
			return m;
		} else return null;
	}
	
	/**
	 * @param minStr
	 * @param maxStr
	 * @param hint
	 */
	private void setRange(String minStr, String maxStr, String hint) {
		if (minStr != null) {
			try {
				min = new Double(minStr);
				defaultMin = min;
			} catch (NumberFormatException x) {
				Log.logError("Non-numeric minimum specified by a metrics extension", x);
			}
		}
		if (maxStr != null) {
			try {
				max = new Double(maxStr);
				defaultMax = max;
			} catch (NumberFormatException x) {
				Log.logError("Non-numeric maximum specified by a metrics extension", x);
			}
		}
		this.hint = hint;
		defaultHint = hint;
		initPreferences();
	}

	/**
	 * workaround for Eclipse ignoring default values in the extension schema
	 * @param element
	 * @param name
	 * @param defaultValue
	 * @return boolean
	 */
	private static boolean getBooleanAttribute(IConfigurationElement element, String name, boolean defaultValue) {
		String val = element.getAttribute(name);
		if (val == null) return defaultValue;
		return "true".equals(val);
	}
	
	/**
	 * get the next higher level from level or null if level is project
	 * @param level
	 * @return String
	 */
	public static String getNextLevel(String level) {
		for (int i = 0; i < levels.length-1;i++) {
			if (levels[i].equals(level)) return levels[i+1];
		}
		return null;
	}
	
	/**
	 * get the next lower level from level or null if level is method
	 * @param level
	 * @return String
	 */
	public static String getPreviousLevel(String level) {
		for (int i = 1; i < levels.length;i++) {
			if (levels[i].equals(level)) return levels[i-1];
		}
		return null;
	}
	
	/**
	 * Get the next higher level from lvl, e.g. method returns type, type
	 * returns compilationUnit, etc. project returns null.
	 * @return String
	 */
	public String getParentLevel() {
		return getNextLevel(level);
	}
	
	
	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the propagateAvg.
	 * @return boolean
	 */
	public boolean isPropagateAvg() {
		return propagateAvg;
	}

	/**
	 * Returns the propagateMax.
	 * @return boolean
	 */
	public boolean isPropagateMax() {
		return propagateMax;
	}

	/**
	 * Returns the propagateSum.
	 * @return boolean
	 */
	public boolean isPropagateSum() {
		return propagateSum;
	}

	public boolean isAllowDisable() {
		return allowDisable;
	}
	
	public String[] getRequiredMetricIds() {
		return requires;
	}
	
	/**
	 * Returns the sumOf.
	 * @return String
	 */
	public String getSumOf() {
		return sumOf;
	}

	public boolean isSum() {
		return (sumOf != null)&&(sumOf.length()>0);
	}
	
	/**
	 * Returns the newAvgMaxAt.
	 * @return String
	 */
	public String getNewAvgMaxAt() {
		return newAvgMaxAt;
	}
	
	/**
	 * Returns the level.
	 * @return String
	 */
	public String getLevel() {
		return level;
	}

	public String toString() {
		return "<metric id=\"" + id + "\" name=\"" + name + "\"/>";
	}
	
	/**
	 * @return String
	 */
	public String getHint() {
		return MetricsPlugin.getDefault().getPreferenceStore().getString(getPrefName("HINT"));
	}

	/**
	 * @return Double
	 */
	public Double getMax() {
		double max = MetricsPlugin.getDefault().getPreferenceStore().getDouble(getPrefName("MAX"));
		if (max == IPreferenceStore.DOUBLE_DEFAULT_DEFAULT) return null;
		return new Double(max);
	}

	/**
	 * @return Double
	 */
	public Double getMin() {
		double min = MetricsPlugin.getDefault().getPreferenceStore().getDouble(getPrefName("MIN"));
		if (min == IPreferenceStore.DOUBLE_DEFAULT_DEFAULT) return null;
		return new Double(min);
	}

	/**
	 * Check if the given value is within the safe range (boundaries inclusive) of this metric
	 * @param value
	 * @return true if value is within safe boundaries (inclusive)
	 */
	public boolean isValueInRange(double value) {
		if ((getMin() != null) && (value < getMin().doubleValue())) return false;
		if ((getMax() != null) && (value > getMax().doubleValue())) return false;
		return true;
	}
	
	/**
	 * @param string
	 */
	public void setHint(String hint) {
		this.hint = hint;		
	}

	/**
	 * @param double1
	 */
	public void setMax(Double max) {
		this.max = max;
	}

	/**
	 * @param double1
	 */
	public void setMin(Double min) {
		this.min = min;
	}
	
	/**
	 * @param string
	 * @return
	 */
	private String getPrefName(String string) {
		StringBuffer b = new StringBuffer("METRICS.RANGE.");
		b.append(getId()).append(".").append(string);
		return b.toString();
	}

	private void initPreferences() {
		if (hint != null) MetricsPlugin.getDefault().getPreferenceStore().setDefault(getPrefName("HINT"), hint);
		if (min != null) MetricsPlugin.getDefault().getPreferenceStore().setDefault(getPrefName("MIN"), min.doubleValue());
		if (max != null) MetricsPlugin.getDefault().getPreferenceStore().setDefault(getPrefName("MAX"), max.doubleValue());
	}
	
	public void copyToPreferences() {
		if (hint != null) MetricsPlugin.getDefault().getPreferenceStore().setValue(getPrefName("HINT"), hint);
		if (min != null) MetricsPlugin.getDefault().getPreferenceStore().setValue(getPrefName("MIN"), min.doubleValue());
		if (max != null) MetricsPlugin.getDefault().getPreferenceStore().setValue(getPrefName("MAX"), max.doubleValue());
	}
	
	public void resetToDefaults() {
		hint = defaultHint;
		max = defaultMax;
		min = defaultMin;
		MetricsPlugin.getDefault().getPreferenceStore().setToDefault(getPrefName("HINT"));
		MetricsPlugin.getDefault().getPreferenceStore().setToDefault(getPrefName("MIN"));
		MetricsPlugin.getDefault().getPreferenceStore().setToDefault(getPrefName("MAX"));
	}

}
