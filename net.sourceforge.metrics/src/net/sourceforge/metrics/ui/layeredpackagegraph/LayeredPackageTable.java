/*
 * Copyright (c) 2003 Frank Sauer. All rights reserved. Licenced under CPL 1.0
 * (Common Public License Version 1.0). The licence is available at
 * http://www.eclipse.org/legal/cpl-v10.html. DISCLAIMER OF WARRANTIES AND
 * LIABILITY: THE SOFTWARE IS PROVIDED "AS IS". THE AUTHOR MAKES NO
 * REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS OR IMPLIED. TO THE EXTENT NOT
 * PROHIBITED BY LAW, IN NO EVENT WILL THE AUTHOR BE LIABLE FOR ANY DAMAGES,
 * INCLUDING WITHOUT LIMITATION, LOST REVENUE, PROFITS OR DATA, OR FOR SPECIAL,
 * INDIRECT, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO ANY
 * FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE
 * AUTHOR HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. $Id:
 * MetricsTable.java,v 1.36 2003/06/14 03:45:13 sauerf Exp $
 */
package net.sourceforge.metrics.ui.layeredpackagegraph;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.IGraphContributor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

/**
 * TableTree specialized for metrics display. Specializations include lazy child creation and child sorting in descending metric value
 * 
 * @author Frank Sauer
 */
public class LayeredPackageTable extends TableTree implements Constants, SelectionListener, TreeListener {
	private Color lastDefaultColor;
	private Color lastInRangeColor;
	private Color lastOutofRangeColor;
	private TableColumn layer;
	private TableColumn value;
	private TableColumn dependencies;
	private Map<String, Set<String>> deps;
	private static List<Set<PackageStats>> layers;
	private static Set<PackageStats> external;

	public LayeredPackageTable(Composite parent, int style) {
		super(parent, style);
		getTable().setLinesVisible(true);
		getTable().setHeaderVisible(true);
		layer = new TableColumn(getTable(), SWT.RIGHT);
		layer.setText("Layer");
		value = new TableColumn(getTable(), SWT.LEFT);
		value.setText("Package");
		dependencies = new TableColumn(getTable(), SWT.LEFT);
		dependencies.setText("Dependent Packages");
		addSelectionListener(this);
		addTreeListener(this);
	}

	/**
	 * Update the table with new dependencies.
	 */
	public void setMetrics(final AbstractMetricSource ms) {
		try {
			removeAll();
			// TODO: Allow for statistics for one package only
			if (!(ms instanceof IGraphContributor)) {
				return;
			}
			deps = ((IGraphContributor) ms).getEfferent();
			if (deps != null) {
				external = new TreeSet<PackageStats>();
				calculateLayers(deps, external);
				displayInternalPackages(deps, layers);
				displayExternalPackages(external);
			}
		} catch (Throwable e) {
			Log.logError("MetricsTable::setMetrics", e);
			e.printStackTrace();
		}
	}

	private void displayExternalPackages(Set external) {
		TableTreeItem divider = createNewRow();
		divider.setText(0, "EXTERNAL");
		divider.setText(1, "");
		divider.setText(2, "");

		for (Iterator j = external.iterator(); j.hasNext();) {
			String packageName = ((PackageStats) j.next()).getPackageName();
			TableTreeItem row = createNewRow();
			row.setText(0, "0");
			row.setText(1, packageName);
			row.setText(2, "");
		}
	}

	/**
	 * @param deps
	 * @param packages
	 */
	private void displayInternalPackages(Map<String, Set<String>> deps, List<Set<PackageStats>> packages) {
		for (int i = packages.size() - 1; i >= 0; i--) {
			Set packageSet = packages.get(i);
			for (Iterator j = packageSet.iterator(); j.hasNext();) {
				PackageStats packageStats = (PackageStats) j.next();
				TableTreeItem row = createNewRow();
				if (packageStats.isTangle()) {
					row.setForeground(getOutOfRangeForeground());
				}
				row.setText(0, "" + i);
				row.setText(1, packageStats.getPackageName());
				String depPackages = "";
				if (deps.get(packageStats.getPackageName()) != null) {
					SortedSet<String> dependencies = new TreeSet<String>(deps.get(packageStats.getPackageName()));
					for (Object element : dependencies) {
						String depPackage = (String) element;
						if (!depPackage.equals(packageStats.getPackageName())) {
							depPackages += (depPackages.length() != 0 ? ", " : "") + depPackage;
						}
					}
				}
				row.setText(2, depPackages);
			}
		}
	}

	private static void calculateLayers(Map<String, Set<String>> deps, Set<PackageStats> external) {
		layers = new ArrayList<Set<PackageStats>>();
		for (Object element : deps.keySet()) {
			String packageName = (String) element;
			PackageStats stats = calculatePackageStat(packageName, deps, new ArrayList<String>(), new HashSet<String>(), external);
			addStat(stats);
		}
	}

	private static void addStat(PackageStats packageStats) {
		while (layers.size() < packageStats.getLayer() + 1) {
			layers.add(new TreeSet<PackageStats>());
		}
		layers.get(packageStats.getLayer()).add(packageStats);
	}

	static PackageStats calculatePackageStat(String packageName, Map<String, Set<String>> deps, List<String> stack, Set<String> tangleStarts, Set<PackageStats> external) {
		PackageStats stats = getStat(packageName);
		// if (stats != null) {
		// return stats;
		// }
		stats = new PackageStats(packageName);
		Set<String> dependecies = deps.get(packageName);
		if (dependecies != null) {
			stack.add(packageName);
			for (Object element : dependecies) {
				String dependentPackageName = (String) element;
				if (!dependentPackageName.equals(packageName)) {
					if (stack.contains(dependentPackageName)) {
						// Tangle!!
						// Mark the start package
						tangleStarts.add(dependentPackageName);
					} else {
						Set<String> childTangles = new HashSet<String>();
						PackageStats childStat = calculatePackageStat(dependentPackageName, deps, stack, childTangles, external);
						if (childTangles.isEmpty()) {
							stats.raiseTo(childStat.getLayer() + 1);
						} else {
							// Child and this node are in the same tangle
							// Do not add 1 for this child
							tangleStarts.addAll(childTangles);
							stats.setTangle();
							stats.raiseTo(childStat.getLayer());
							if (tangleStarts.contains(packageName)) {
								// This package is the start of a tangle
								tangleStarts.remove(packageName);
							}
						}
					}
				}
			}
			stack.remove(stack.size() - 1);
		} else {
			external.add(stats);
		}
		return stats;
	}

	private void setForeground(Metric metric, TableTreeItem row) {
		if (metric == null) {
			row.setForeground(getDefaultForeground());
		} else {
			MetricDescriptor md = MetricsPlugin.getDefault().getMetricDescriptor(metric.getName());
			Color c = md.isValueInRange(metric.doubleValue()) ? getInRangeForeground() : getOutOfRangeForeground();
			row.setForeground(c);
		}
	}

	/**
	 * create a new root row
	 * 
	 * @return
	 */
	private TableTreeItem createNewRow() {
		TableTreeItem item = new TableTreeItem(this, SWT.NONE);
		item.setForeground(getDefaultForeground());
		return item;
	}

	/**
	 * create a new child row
	 * 
	 * @param parent
	 * @return
	 */
	private TableTreeItem createNewRow(TableTreeItem parent) {
		TableTreeItem item = new TableTreeItem(parent, SWT.NONE);
		item.setForeground(getDefaultForeground());
		return item;
	}

	/**
	 * Get the default color (for metrics without a max and within range)
	 * 
	 * @return
	 */
	private Color getDefaultForeground() {
		RGB color = PreferenceConverter.getColor(MetricsPlugin.getDefault().getPreferenceStore(), "METRICS.defaultColor");
		if (lastDefaultColor == null) {
			lastDefaultColor = new Color(getDisplay(), color);
		} else if (!lastDefaultColor.getRGB().equals(color)) {
			lastDefaultColor.dispose();
			lastDefaultColor = new Color(getDisplay(), color);
		}
		return lastDefaultColor;
	}

	/**
	 * Get the in range color (for metrics with a max and within range)
	 * 
	 * @return
	 */
	private Color getInRangeForeground() {
		RGB color = PreferenceConverter.getColor(MetricsPlugin.getDefault().getPreferenceStore(), "METRICS.linkedColor");
		if (lastInRangeColor == null) {
			lastInRangeColor = new Color(getDisplay(), color);
		} else if (!lastInRangeColor.getRGB().equals(color)) {
			lastInRangeColor.dispose();
			lastInRangeColor = new Color(getDisplay(), color);
		}
		return lastInRangeColor;
	}

	/**
	 * Get the out of range color (for metrics with a max and out of range)
	 * 
	 * @return
	 */
	private Color getOutOfRangeForeground() {
		RGB color = PreferenceConverter.getColor(MetricsPlugin.getDefault().getPreferenceStore(), "METRICS.outOfRangeColor");
		if (lastOutofRangeColor == null) {
			lastOutofRangeColor = new Color(getDisplay(), color);
		} else if (!lastOutofRangeColor.getRGB().equals(color)) {
			lastOutofRangeColor.dispose();
			lastOutofRangeColor = new Color(getDisplay(), color);
		}
		return lastOutofRangeColor;
	}

	/**
	 * first time simply adds a dummy child if there should be any children. When such a dummy child is already rpesent, replaces it with the real children. This spreads the work load across expansion events instead of constructing the
	 * entire tree up front, which could be very time consuming in large projects.
	 * 
	 * @param row
	 *            parent TableTreeItem
	 * @param ms
	 *            AbstractMetricSource containing the metrics
	 * @param metric
	 *            Name of the Metric
	 * @param per
	 *            name of the avg/max type
	 */
	private void addChildren(TableTreeItem row, AbstractMetricSource ms, String metric, String per) {
		AbstractMetricSource[] children = ms.getChildrenHaving(per, metric);
		// don't have to do anything if there are no child metrics
		if ((children != null) && (children.length > 0)) {
			if (row.getData("source") != null) {
				// dummy already present, replace it with the real thing
				row.setData("source", null);
				row.getItems()[0].dispose();
				sort(children, metric, per);
				for (AbstractMetricSource element : children) {
					TableTreeItem child = createNewRow(row);
					child.setText(getElementName(element.getJavaElement()));
					Metric val = element.getValue(metric);
					child.setText(1, (val != null) ? format(val.doubleValue()) : "");
					Avg avg = element.getAverage(metric, per);
					child.setText(2, (avg != null) ? format(avg.doubleValue()) : "");
					child.setText(3, (avg != null) ? format(avg.getStandardDeviation()) : "");
					Max max = element.getMaximum(metric, per);
					child.setText(4, (max != null) ? format(max.doubleValue()) : "");
					if (max != null) {
						IJavaElement maxElm = JavaCore.create(max.getHandle());
						child.setText(5, getPath(maxElm));
						child.setText(6, getMethodName(maxElm));
						setForeground(max, child);
					} else {
						child.setText(5, "");
						child.setText(6, "");
						if (val != null) {
							setForeground(val, row);
						}
					}
					child.setData("handle", element.getHandle());
					child.setData("element", element.getJavaElement());
					// recurse
					addChildren(child, element, metric, per);
				}
			} else { // add dummy
				/* TableTreeItem child = */createNewRow(row);
				row.setData("metric", metric);
				row.setData("per", per);
				row.setData("source", ms);
			}
		} else {
			Max max = ms.getMaximum(metric, per);
			if (max != null) {
				setForeground(max, row);
			} else {
				setForeground(ms.getValue(metric), row);
			}
		}
	}

	private String getElementName(IJavaElement element) {
		String candidate = element.getElementName();
		if ("".equals(candidate)) {
			return "(default package)";
		}
		return candidate;
	}

	/**
	 * Sort the metrics in descending max/value order, giving preference to max over value (if max exists, use it, otherwise use value)
	 * 
	 * @param children
	 * @param metric
	 * @param per
	 * @return
	 */
	private void sort(AbstractMetricSource[] children, final String metric, final String per) {
		Comparator<AbstractMetricSource> c = new Comparator<AbstractMetricSource>() {

			public int compare(AbstractMetricSource o1, AbstractMetricSource o2) {
				Max max1 = o1.getMaximum(metric, per);
				Max max2 = o2.getMaximum(metric, per);
				if ((max1 != null) && (max2 != null)) {
					return -max1.compareTo(max2);
				} /* else { */
				Metric m1 = o1.getValue(metric);
				Metric m2 = o2.getValue(metric);
				if ((m1 != null) && (m2 != null)) {
					return -m1.compareTo(m2);
				} /* else { */
				if ((max1 != null) && (max2 == null)) {
					return -1;
				}
				if ((m1 != null) && (m2 == null)) {
					return -1; // replaced from if ((m2 != null) && (m2
					// == null)) return -1;
				}
				return 1;
				/* } */
				/* } */
			}
		};
		Arrays.sort(children, c);
	}

	/*
	 * private void setText(TableTreeItem row, String[] columns) { row.setText(columns[0]); for (int i = 1; i < columns.length; i++) { row.setText(i, columns[i]); } }
	 */

	/**
	 * @param handle
	 * @return String
	 */
	private String getMethodName(IJavaElement element) {
		return (element.getElementType() == IJavaElement.METHOD) ? element.getElementName() : "";
	}

	/**
	 * @param handle
	 * @return String
	 */
	private String getPath(IJavaElement element) {
		return element.getPath().toString();
	}

	/**
	 * Method getCompilationUnit.
	 * 
	 * @param source
	 * @return Object
	 */
	// private ICompilationUnit getCompilationUnit(String path) {
	// IResource source = ResourcesPlugin.getWorkspace().getRoot().findMember(
	// path);
	// if (source.getType() == IResource.FILE) {
	// return JavaCore.createCompilationUnitFrom((IFile) source);
	// }
	// return null;
	// }

	private String format(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		int decimals = MetricsPlugin.getDefault().getPreferenceStore().getInt("METRICS.decimals");
		nf.setMaximumFractionDigits(decimals);
		nf.setGroupingUsed(false);
		return nf.format(value);
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#checkSubclass()
	 */
	@Override
	protected void checkSubclass() {
	}

	public void widgetSelected(SelectionEvent e) {
	}

	/**
	 * react to double-clicks in the table by opening an editor on the resource for the metric
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		TableTreeItem row = (TableTreeItem) e.item;
		IJavaElement element = (IJavaElement) row.getData("element");
		String handle = (String) row.getData("handle");
		try {
			if (element != null) {
				IEditorPart javaEditor = JavaUI.openInEditor(element);
				if (element instanceof IMember) {
					JavaUI.revealInEditor(javaEditor, element);
				}
			}
		} catch (PartInitException x) {
			System.err.println("Error selecting " + handle);
			x.printStackTrace();
		} catch (JavaModelException x) {
			System.err.println("Error selecting " + handle);
			x.printStackTrace();
		} catch (Throwable t) {
			System.err.println("Error selecting " + handle);
			t.printStackTrace();
		}
	}

	private int getWidth(IMemento m, String name, int defaultVal) {
		try {
			Integer val = m.getInteger(name);
			return (val == null) ? defaultVal : val.intValue();
		} catch (Throwable e) {
			return defaultVal;
		}
	}

	void initWidths(IMemento memento) {
		layer.setWidth(getWidth(memento, "description", 70));
		value.setWidth(getWidth(memento, "value", 250));
		dependencies.setWidth(getWidth(memento, "dependencies", 1250));
	}

	void updateWidths(IMemento memento) {
		memento.putInteger("description", layer.getWidth());
		memento.putInteger("value", value.getWidth());
		memento.putInteger("dependencies", dependencies.getWidth());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(org.eclipse.swt.events .TreeEvent)
	 */
	public void treeCollapsed(TreeEvent e) {
	}

	/**
	 * replaces dummy child nodes with the real children if needed
	 */
	public void treeExpanded(TreeEvent e) {
		TableTreeItem item = (TableTreeItem) e.item;
		if (item.getData("source") != null) {
			AbstractMetricSource source = (AbstractMetricSource) item.getData("source");
			String metric = (String) item.getData("metric");
			String per = (String) item.getData("per");
			addChildren(item, source, metric, per);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (lastDefaultColor != null) {
			lastDefaultColor.dispose();
		}
		if (lastInRangeColor != null) {
			lastInRangeColor.dispose();
		}
		if (lastOutofRangeColor != null) {
			lastOutofRangeColor.dispose();
		}
	}

	public static List getLayers() {
		return layers;
	}

	public static int getLayer(String depPackageName) {
		PackageStats stats = getStat(depPackageName);

		if (stats != null) {
			return stats.getLayer();
		}
		if (depPackageName.startsWith("knot")) {
			return 0;
		}
		throw new RuntimeException("Unknown package: \"" + depPackageName + "\"");
	}

	private static PackageStats getStat(String packageName) {
		for (Object element : layers) {
			Set packageSet = (Set) element;
			for (Iterator j = packageSet.iterator(); j.hasNext();) {
				PackageStats stats = (PackageStats) j.next();
				if (stats.getPackageName().equals(packageName)) {
					return stats;
				}
			}
		}

		for (Object element : external) {
			PackageStats stats = (PackageStats) element;
			if (stats.getPackageName().equals(packageName)) {
				return stats;
			}
		}

		return null;
	}

}