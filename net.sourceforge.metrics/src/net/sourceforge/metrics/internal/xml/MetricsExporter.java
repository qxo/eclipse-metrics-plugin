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
 * $id$
 */
package net.sourceforge.metrics.internal.xml;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Frank Sauer
 */
public abstract class MetricsExporter implements Constants {

	private static NumberFormat nf;
	private static String[] attributeNames = new String[] {
		"id", "value", "avg", "stddev", "points","max", "per", "maxhandle"
	};
	
	public MetricsExporter() {
	}
	
	private static String format(double value) {
		if (nf == null) {
			nf = NumberFormat.getInstance();
			int decimals = MetricsPlugin.getDefault().getPreferenceStore().getInt("METRICS.decimals");
			nf.setMaximumFractionDigits(decimals);
			nf.setGroupingUsed(false);
		}
		return nf.format(value);
	}

	protected void printMetrics(AbstractMetricSource source, XMLPrintStream out, int level) {
		out.indent(level);
		out.println("<Metrics>");
		MetricsPlugin plugin = MetricsPlugin.getDefault();
		String[] names = plugin.getMetricIds(); 
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			Properties attributes = getAttributes(source, name);
			if (attributes != null) {
				out.indent(level+1);
				out.print("<Metric");
				printAttributes(out, attributes);
				out.println("/>");
			}
		}		
		
		out.indent(level);
		out.println("</Metrics>");
	}

	/**
	 * @param out
	 * @param attributes
	 */
	protected void printAttributes(XMLPrintStream out, Properties attributes) {
		for (int i = 0; i < attributeNames.length; i++) {
			String name = attributeNames[i] ;
			String value = attributes.getProperty(name);
			if (value != null) {
				out.print(' ');
				out.print(name);
				out.print(" =\"");
				out.print(value);
				out.print('"');
			}
		}
	}

	protected String formatHandle(String handle) {
		// the following is JDK1.4 specific
		//return handle.replaceAll("<", "&lt;");
		StringTokenizer str = new StringTokenizer(handle, "<");
		StringBuffer b = new StringBuffer();
		while (str.hasMoreTokens()) {
			b.append(str.nextToken());
			b.append("&lt;");
		}
		int lastEntity = b.toString().lastIndexOf("&lt;");
		return b.substring(0,lastEntity);
	}
	
	/**
	 * @param source
	 * @param name
	 */
	protected Properties getAttributes(AbstractMetricSource source, String name) {
		Properties p = new Properties();
		Metric m = source.getValue(name);
		if (m != null) {
			p.put("value",format(m.doubleValue()));
		}
		for (int j = 0; j < pers.length;j++) {
			Avg avg = source.getAverage(name, pers[j]);
			Max max = source.getMaximum(name, pers[j]);
			if ((avg != null)||(max != null)) {
				p.put("per", pers[j]);
				if (avg != null) {
					p.put("avg", format(avg.doubleValue()));
					p.put("stddev",format(avg.getStandardDeviation()));
					p.put("points", String.valueOf(avg.getPoints()));
				}
				if (max != null) {
					p.put("max",format(max.doubleValue()));
					p.put("maxhandle", formatHandle(max.getHandle()));
				}
			}
		}
		if (p.size()>0) {
			p.put("id", name);
			return p;
		} else return null;
	}

	/**
		 * @param out
		 * @param elements
		 * @param i
		 */
	protected void exportChildren(XMLPrintStream out, String handle, int level, Class filter, IProgressMonitor monitor) {
		List children = XMLSourceTreeExporter.getChildren(handle, filter);
		for (Iterator i = children.iterator();i.hasNext();) {
			AbstractMetricSource next = (AbstractMetricSource)i.next();
			if (filter.isInstance(next)) {
				IXMLExporter exporter = next.getExporter();
				exporter.export(next, out, level, monitor);
				monitor.worked(1);
			}			
		}
	}

	protected void printOpeningTag(AbstractMetricSource source, XMLPrintStream out, int level) {
		IJavaElement element = source.getJavaElement();
		out.indent(level);
		out.print('<');
		out.print(getTagName());
		out.print(" name = \"");
		out.print(getElementName(element));
		out.print("\" handle = \"");
		out.print(formatHandle(source.getHandle()));
		out.println("\">");
	}

	protected String getElementName(IJavaElement element) {
		String name = element.getElementName();
		if ("".equals(name)) name = "(default package)";
		return name;
	}
	
	public abstract String getTagName();

	protected void printClosingTag(XMLPrintStream out, int level) {
		out.indent(level);
		out.print("</");
		out.print(getTagName());
		out.println(">");
	}
}
