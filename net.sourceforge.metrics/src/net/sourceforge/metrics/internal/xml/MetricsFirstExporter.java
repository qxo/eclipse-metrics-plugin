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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.IExporter;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Cache;
import net.sourceforge.metrics.core.sources.MethodMetrics;
import net.sourceforge.metrics.core.sources.PackageFragmentMetrics;
import net.sourceforge.metrics.core.sources.PackageFragmentRootMetrics;
import net.sourceforge.metrics.core.sources.ProjectMetrics;
import net.sourceforge.metrics.core.sources.TypeMetrics;
import net.sourceforge.metrics.ui.dependencies.PackageAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import classycle.graph.AtomicVertex;
import classycle.graph.StrongComponent;
import classycle.graph.StrongComponentProcessor;
import classycle.graph.Vertex;

/**
 * Export metrics in the http://metrics.sourceforge.net/2003/Metrics-First-Flat format (flat per metric data ideal for plotting graphs and histograms)
 * 
 * @author Frank Sauer
 */
public class MetricsFirstExporter implements IExporter, Constants {

	private SoftCache<String, AbstractMetricSource> cache;
	private MetricsPlugin plugin = MetricsPlugin.getDefault();
	private String[] names = plugin.getMetricIds();
	private String[] descriptions = plugin.getMetricDescriptions();

	public MetricsFirstExporter() {
		cache = new SoftCache<String, AbstractMetricSource>();
	}

	protected String formatXMLStr(String handle) {
		return handle.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	public void export(IJavaElement element, File outputFile, IProgressMonitor monitor) throws InvocationTargetException {
		try {
			FileOutputStream out = new FileOutputStream(outputFile);
			XMLPrintStream pOut = new XMLPrintStream(out);
			monitor.beginTask("Exporting metrics to flat per Metric XML format...", names.length);
			AbstractMetricSource root = getData(element);
			pOut.printXMLHeader();
			NumberFormat nf = NumberFormat.getInstance();
			int decimals = MetricsPlugin.getDefault().getPreferenceStore().getInt("METRICS.decimals");
			nf.setMaximumFractionDigits(decimals);
			nf.setGroupingUsed(false);
			printRoot(root, pOut, monitor, nf);
			pOut.close();
		} catch (FileNotFoundException e) {
			throw new InvocationTargetException(e);
		}
	}

	protected AbstractMetricSource getData(IJavaElement element) {
		return getData(element.getHandleIdentifier());
	}

	/*
	 * BUG 756998 This extra caching helps a LOT!!!!!!!
	 */
	protected AbstractMetricSource getData(String handle) {
		AbstractMetricSource s = cache.get(handle);
		if (s == null) {
			s = Cache.singleton.get(handle);
			cache.put(handle, s);
		}
		return s;
	}

	private void printRoot(AbstractMetricSource root, XMLPrintStream pOut, IProgressMonitor monitor, NumberFormat nf) {
		pOut.print("<Metrics scope=\"");
		pOut.print(getName(root));
		pOut.print("\" type=\"");
		pOut.print(root.getExporter().getTagName());
		pOut.print("\" date=\"");
		pOut.print(pOut.formatXSDDate(new Date()));
		pOut.println("\" xmlns=\"http://metrics.sourceforge.net/2003/Metrics-First-Flat\">");
		maybePrintCycles(root, pOut, monitor);
		for (int i = 0; i < names.length; i++) {
			monitor.subTask("Exporting: " + descriptions[i]);
			MetricDescriptor md = plugin.getMetricDescriptor(names[i]);
			pOut.indent(1);
			pOut.print("<Metric ");
			pOut.print("id = \"");
			pOut.print(names[i]);
			pOut.print("\" ");
			pOut.print("description =\"");
			pOut.print(descriptions[i]);
			pOut.print("\"");
			Double max = md.getMax();
			if (max != null) {
				pOut.print(" max =\"");
				pOut.print(nf.format(max.doubleValue()));
				pOut.print("\"");
			}
			String hint = md.getHint();
			if (hint != null && hint.length() > 0) {
				pOut.print(" hint =\"");
				pOut.print(hint);
				pOut.print("\"");
			}
			pOut.println(">");
			if (!printValues(names[i], root, pOut, nf)) {
				Metric val = root.getValue(names[i]);
				if (val != null) {
					pOut.indent(2);
					pOut.print("<Value value=\"");
					pOut.print(nf.format(val.doubleValue()));
					pOut.println("\"/>");
				}
			}
			pOut.indent(1);
			pOut.println("</Metric>");
			monitor.worked(1);
		}
		pOut.indent(1);
		pOut.println("</Metrics>");
		monitor.done();
	}

	/**
	 * If root is a source folder or a project, include any cyclic package dependencies in the xml
	 * 
	 * @param root
	 * @param pOut
	 * @param monitor
	 */
	private void maybePrintCycles(AbstractMetricSource root, XMLPrintStream pOut, IProgressMonitor monitor) {
		Map dependencies = null;
		if (root.getLevel() == Constants.PACKAGEROOT) {
			PackageFragmentRootMetrics pfr = (PackageFragmentRootMetrics) root;
			dependencies = pfr.getEfferent();
		}
		if (root.getLevel() == Constants.PROJECT) {
			ProjectMetrics pm = (ProjectMetrics) root;
			dependencies = pm.getEfferent();
		}
		if (dependencies != null) {
			monitor.subTask("Exporting cyclic dependencies...");
			StrongComponent[] comps = calculateCycles(dependencies);
			for (StrongComponent comp : comps) {
				exportCycle(comp, pOut);
			}
		}
	}

	/**
	 * Write a single cycle (if length > 1) to XML
	 * 
	 * @param component
	 * @param pOut
	 */
	private void exportCycle(StrongComponent component, XMLPrintStream pOut) {
		int nodes = component.getNumberOfVertices();
		if (nodes > 1) {
			int diameter = component.getDiameter();
			String name = component.getVertex(0).getAttributes().toString() + " et al";
			pOut.indent(1);
			pOut.print("<Cycle name=\"");
			pOut.print(name);
			pOut.print("\" nodes=\"");
			pOut.print(nodes);
			pOut.print("\" diameter=\"");
			pOut.print(diameter);
			pOut.println("\">");
			for (int i = 0; i < nodes; i++) {
				String packName = component.getVertex(i).getAttributes().toString();
				pOut.indent(2);
				pOut.print("<Package>");
				pOut.print(packName);
				pOut.println("</Package>");
			}
			pOut.indent(1);
			pOut.println("</Cycle>");
		}
	}

	// TODO refactor to common place between this and DependencyGraphPanel
	private StrongComponent[] calculateCycles(Map efferent) {
		List<Vertex> graph = new ArrayList<Vertex>();
		Map<String, Vertex> done = new HashMap<String, Vertex>();
		for (Iterator i = efferent.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Vertex from = done.get(key);
			if (from == null) {
				from = new AtomicVertex(new PackageAttributes(key));
				done.put(key, from);
				graph.add(from);
			}
			Set deps = (Set) efferent.get(key);
			for (Iterator j = deps.iterator(); j.hasNext();) {
				String dep = (String) j.next();
				Vertex to = done.get(dep);
				if (to == null) {
					to = new AtomicVertex(new PackageAttributes(dep));
					done.put(dep, to);
					graph.add(to);
				}
				from.addOutgoingArcTo(to);
			}
		}
		Vertex[] vgraph = graph.toArray(new Vertex[] {});
		StrongComponentProcessor scp = new StrongComponentProcessor();
		scp.deepSearchFirst(vgraph);
		StrongComponent[] comps = scp.getStrongComponents();
		return comps;
	}

	private Class[] filters = new Class[] { PackageFragmentMetrics.class, TypeMetrics.class, MethodMetrics.class };

	private boolean printValues(String id, AbstractMetricSource element, XMLPrintStream pOut, NumberFormat nf) {
		boolean result = false;
		for (int i = 0; i < pers.length; i++) {
			Metric total = element.getValue(id);
			MetricDescriptor md = plugin.getMetricDescriptor(id);
			Avg avg = element.getAverage(id, pers[i]);
			Max max = element.getMaximum(id, pers[i]);
			if ((avg != null) || (max != null)) {
				pOut.indent(2);
				pOut.print("<Values per = \"");
				pOut.print(pers[i]);
				pOut.print("\"");
				if (total != null) {
					pOut.print(" total = \"");
					pOut.print(nf.format(total.doubleValue()));
					pOut.print("\"");
				}
				if (avg != null) {
					pOut.print(" avg = \"");
					pOut.print(nf.format(avg.doubleValue()));
					pOut.print("\"");
					pOut.print(" stddev = \"");
					pOut.print(nf.format(avg.getStandardDeviation()));
					pOut.print("\"");
				}
				if (max != null) {
					pOut.print(" max = \"");
					pOut.print(nf.format(max.doubleValue()));
					pOut.print("\"");
					if (!md.isValueInRange(max.doubleValue())) {
						pOut.print(" maxinrange=\"false\"");
					}
				}
				pOut.println(">");
				List<AbstractMetricSource> values = getChildren(element.getHandle(), filters[i]);
				printValues(values, id, pOut, md, nf);
				pOut.indent(2);
				pOut.println("</Values>");
				result = true;
			}
		}
		return result;
	}

	/**
	 * @param values
	 * @param id
	 * @param pOut
	 */
	private void printValues(List<AbstractMetricSource> values, final String id, XMLPrintStream pOut, MetricDescriptor md, NumberFormat nf) {
		// sort values first
		Collections.sort(values, new Comparator<? super AbstractMetricSource>() {

			public int compare(Object o1, Object o2) {
				AbstractMetricSource left = (AbstractMetricSource) o1;
				AbstractMetricSource right = (AbstractMetricSource) o2;
				Metric lm = left.getValue(id);
				Metric rm = right.getValue(id);
				int result;
				if (lm == null) { // BUG #826997
					result = (rm == null) ? 0 : -1;
				} else {
					result = -lm.compareTo(rm); // BUG 746394
				}
				if (result != 0) {
					return result;
				}
				return left.getHandle().compareTo(right.getHandle());
			}

		});
		for (AbstractMetricSource next : values) {
			IJavaElement element = next.getJavaElement();
			Metric val = next.getValue(id);
			if (val != null) {
				pOut.indent(3);
				pOut.print("<Value name=\"");
				pOut.print(getName(element));
				pOut.print("\" ");
				IJavaElement source = element.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (source != null) {
					pOut.print("source =\"");
					pOut.print(getName(source));
					pOut.print("\" ");
				}
				IJavaElement packageF = element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
				if (packageF != null) {
					pOut.print("package =\"");
					pOut.print(getName(packageF));
					pOut.print("\" ");
				}
				pOut.print("value =\"");
				pOut.print(nf.format(val.doubleValue()));
				pOut.print("\"");
				if (!md.isValueInRange(val.doubleValue())) {
					pOut.print(" inrange=\"false\"");
				}
				pOut.println("/>");
			}
		}
	}

	protected List<AbstractMetricSource> getChildren(String handle, Class filter) {
		List<String> handles = new ArrayList<String>(Cache.singleton.getKeysForHandle(handle));
		List<AbstractMetricSource> result = new ArrayList<AbstractMetricSource>();
		for (String next : handles) {
			if (next.startsWith(handle) && (!next.equals(handle))) {
				AbstractMetricSource p = getData(next);
				if (filter.isInstance(p)) {
					result.add(p);
				}
			}
		}
		return result;
	}

	public String getTagName() {
		return null;
	}

	protected String getName(AbstractMetricSource source) {
		return getName(source.getJavaElement());
	}

	protected String getName(IJavaElement element) {
		return formatXMLStr(getNotBlankName(buildName(element), element));
	}

	protected String getNotBlankName(String currentName, IJavaElement element) {
		String l_return = currentName;
		if ("".equals(l_return)) {
			if (element instanceof IType) {
				IJavaElement parentType = element.getParent().getAncestor(IJavaElement.TYPE);
				String handle = element.getHandleIdentifier();
				int start = handle.lastIndexOf(parentType.getElementName());
				if (start != -1) {
					handle = handle.substring(start + parentType.getElementName().length());
				}
				l_return = "anonymous#" + handle;
			} else {
				l_return = "(default package)";
			}
		}
		return l_return;
	}

	protected String buildName(IJavaElement element) {
		String l_return = element.getElementName();
		if (element instanceof IType) {
			IJavaElement container = element.getParent();
			if (container != null && container.getAncestor(IJavaElement.TYPE) != null) {
				l_return = buildParentTypeNamePart(element);
			}
		} else if (element instanceof IMethod) {
			IJavaElement container = element.getAncestor(IJavaElement.TYPE);
			if (container != null && container.getParent() != null && container.getParent().getAncestor(IJavaElement.TYPE) != null) {
				l_return = buildParentTypeNamePart(container) + "#" + element.getElementName();
			}
		}
		return l_return;
	}

	protected String buildParentTypeNamePart(IJavaElement element) {
		StringBuffer l_strBuffer = new StringBuffer(getNotBlankName(element.getElementName(), element));
		IJavaElement l_current = element.getParent().getAncestor(IJavaElement.TYPE);
		while (l_current != null) {
			l_strBuffer.insert(0, '.');
			l_strBuffer.insert(0, getNotBlankName(l_current.getElementName(), l_current));
			l_current = l_current.getParent();
			if (l_current != null) {
				l_current = l_current.getAncestor(IJavaElement.TYPE);
			}
		}
		return l_strBuffer.toString();
	}

	/**
	 * keys are strong, values are soft, strangely enough WeakHashMap does the opposite??? What good is that?
	 * 
	 * @author Frank Sauer
	 */
	static class SoftCache<K, V> {

		private Map<K, Reference<V>> inner;

		SoftCache() {
			inner = new HashMap<K, Reference<V>>();
		}

		public V put(K key, V value) {
			Reference<V> old = inner.put(key, new SoftReference<V>(value));
			return old == null ? null : old.get();
		}

		public V get(K key) {
			Reference<V> r = inner.get(key);
			return r == null ? null : r.get();
		}

	}
}
