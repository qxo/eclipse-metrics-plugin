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

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

/**
 * @author Frank Sauer
 */
public abstract class MetricsExporter implements Constants {

	private static NumberFormat nf;
	private static String[] attributeNames = new String[] { "id", "value", "avg", "stddev", "points", "max", "per", "maxhandle" };

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
		for (String name : names) {
			Properties attributes = getAttributes(source, name);
			if (attributes != null) {
				out.indent(level + 1);
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
		for (String name : attributeNames) {
			String value = attributes.getProperty(name);
			if (value != null) {
				out.print(' ');
				out.print(formatAttribut(name));
				out.print(" =\"");
				out.print(formatAttribut(value));
				out.print('"');
			}
		}
	}

	protected String formatHandle(String handle) {
		// the following is JDK1.4 specific
		return handle.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	protected String formatAttribut(String value) {
		return formatHandle(value);
	}

	/**
	 * @param source
	 * @param name
	 */
	protected Properties getAttributes(AbstractMetricSource source, String name) {
		Properties p = new Properties();
		Metric m = source.getValue(name);
		if (m != null) {
			p.put("value", format(m.doubleValue()));
		}
		for (String per : PER_ARRAY) {
			Avg avg = source.getAverage(name, per);
			Max max = source.getMaximum(name, per);
			if ((avg != null) || (max != null)) {
				p.put("per", per);
				if (avg != null) {
					p.put("avg", format(avg.doubleValue()));
					p.put("stddev", format(avg.getStandardDeviation()));
					p.put("points", String.valueOf(avg.getPoints()));
				}
				if (max != null) {
					p.put("max", format(max.doubleValue()));
					p.put("maxhandle", formatHandle(max.getHandle()));
				}
			}
		}
		if (p.size() > 0) {
			p.put("id", name);
			return p;
		} /* else { */
		return null;
		/* } */
	}

	/**
	 * @param out
	 * @param elements
	 * @param i
	 */
	protected void exportChildren(XMLPrintStream out, String handle, int level, Class<? extends AbstractMetricSource> filter, IProgressMonitor monitor) {
		List<AbstractMetricSource> children = XMLSourceTreeExporter.getChildren(handle, filter);
		for (Iterator<AbstractMetricSource> i = children.iterator(); i.hasNext();) {
			AbstractMetricSource next = i.next();
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
		out.print(formatHandle(getElementName(element)));
		out.print("\" handle = \"");
		out.print(formatHandle(source.getHandle()));
		out.println("\">");
	}

	protected String getElementName(IJavaElement element) {
		String name = element.getElementName();
		if ("".equals(name)) {
			if (element instanceof IType) {
				name = "anonymous";
			} else {
				name = "(default package)";
			}
		}
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
