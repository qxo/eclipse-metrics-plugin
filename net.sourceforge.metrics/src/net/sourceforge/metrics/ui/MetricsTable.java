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
 * $Id: MetricsTable.java,v 1.39 2005/02/28 03:07:53 sauerf Exp $
 */
package net.sourceforge.metrics.ui;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

/**
 * TableTree specialized for metrics display. Specializations include lazy child creation and child sorting in descending metric value
 * 
 * @author Frank Sauer
 */
public class MetricsTable extends Tree implements Constants, SelectionListener, TreeListener {

	private Color lastDefaultColor;
	private Color lastInRangeColor;
	private Color lastOutofRangeColor;
	private TreeColumn description;
	private TreeColumn value;
	private TreeColumn average;
	private TreeColumn variance;
	private TreeColumn max;
	private TreeColumn path;
	private TreeColumn method;

	/**
	 * Constructor for MetricsTable.
	 * 
	 * @param parent
	 * @param style
	 */
	public MetricsTable(Composite parent, int style) {
		super(parent, style);
		setLinesVisible(true);
		setHeaderVisible(true);
		description = new TreeColumn(this, SWT.LEFT);
		description.setText("Metric");
		value = new TreeColumn(this, SWT.RIGHT);
		value.setText("Total");
		average = new TreeColumn(this, SWT.RIGHT);
		average.setText("Mean");
		variance = new TreeColumn(this, SWT.RIGHT);
		variance.setText("Std. Dev.");
		max = new TreeColumn(this, SWT.RIGHT);
		max.setText("Maximum");
		path = new TreeColumn(this, SWT.LEFT);
		path.setText("Resource causing Maximum");
		method = new TreeColumn(this, SWT.LEFT);
		method.setText("Method");
		addSelectionListener(this);
		addTreeListener(this);
	}

	/**
	 * Update the table with new metrics.
	 * 
	 * @param ms
	 */
	public void setMetrics(final AbstractMetricSource ms) {
		try {
			removeAll();
			if (ms == null) {
				return;
			}
			MetricsPlugin plugin = MetricsPlugin.getDefault();
			String[] names = plugin.getMetricIds();
			String[] descriptions = plugin.getMetricDescriptions();
			for (int i = 0; i < names.length; i++) {
				boolean rowNeeded = false;
				String name = names[i];
				String[] cols = new String[] { descriptions[i], "", "", "", "", "", "" };
				Metric m = ms.getValue(name);
				if (m != null) {
					rowNeeded = true;
					cols[1] = format(m.doubleValue());
				}
				for (String per : PER_ARRAY) {
					Avg avg = ms.getAverage(name, per);
					Max max = ms.getMaximum(name, per);
					if ((avg != null) || (max != null)) {
						TreeItem row = createNewRow();
						cols[0] = descriptions[i] + " (avg/max per " + per + ")";
						if (avg != null) {
							cols[2] = format(avg.doubleValue());
							cols[3] = format(avg.getStandardDeviation());
						}
						if (max != null) {
							cols[4] = format(max.doubleValue());
							String handle = max.getHandle();
							if (handle != null) {
								IJavaElement element = JavaCore.create(handle);
								cols[5] = getPath(element);
								setForeground(max, row);
								cols[6] = getMethodName(element);
								row.setData("handle", handle);
								row.setData("element", element);
							}
						} else {
							setForeground(m, row);
						}
						setText(row, cols);
						addChildren(row, ms, name, per);
						rowNeeded = false;
					}
				}
				if (rowNeeded) {
					TreeItem row = createNewRow();
					setForeground(m, row);
					setText(row, cols);
					addChildren(row, ms, name, "");
				}
			}
		} catch (Throwable e) {
			Log.logError("MetricsTable::setMetrics", e);
		}
	}

	private void setForeground(Metric metric, TreeItem row) {
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
	private TreeItem createNewRow() {
		TreeItem item = new TreeItem(this, SWT.NONE);
		item.setForeground(getDefaultForeground());
		return item;
	}

	/**
	 * create a new child row
	 * 
	 * @param parent
	 * @return
	 */
	private TreeItem createNewRow(TreeItem parent) {
		TreeItem item = new TreeItem(parent, SWT.NONE);
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
	 *            parent TreeItem
	 * @param ms
	 *            AbstractMetricSource containing the metrics
	 * @param metric
	 *            Name of the Metric
	 * @param per
	 *            name of the avg/max type
	 */
	private void addChildren(TreeItem row, AbstractMetricSource ms, String metric, String per) {
		AbstractMetricSource[] children = ms.getChildrenHaving(per, metric);
		// don't have to do anything if there are no child metrics
		if ((children != null) && (children.length > 0)) {
			if (row.getData("source") != null) {
				// dummy already present, replace it with the real thing
				row.setData("source", null);
				row.getItems()[0].dispose();
				sort(children, metric, per);
				for (AbstractMetricSource element : children) {
					TreeItem child = createNewRow(row);
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
				createNewRow(row);
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
			if (element instanceof IType) {
				return "anonymous";
			}
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
					return -1; // change from if ((m2 != null)&&(m2 ==
					// null)) return -1;
				}
				return 1;
				/* } */
				/* } */
			}
		};
		Arrays.sort(children, c);
	}

	private void setText(TreeItem row, String[] columns) {
		row.setText(columns[0]);
		for (int i = 1; i < columns.length; i++) {
			row.setText(i, columns[i]);
		}
	}

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
		TreeItem row = (TreeItem) e.item;
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
			Log.logError("Error selecting " + handle, x);
			x.printStackTrace();
		} catch (JavaModelException x) {
			Log.logError("Error selecting " + handle, x);
			x.printStackTrace();
		} catch (Throwable t) {
			Log.logError("Error selecting " + handle, t);
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
		description.setWidth(getWidth(memento, "description", 270));
		value.setWidth(getWidth(memento, "value", 50));
		average.setWidth(getWidth(memento, "average", 50));
		variance.setWidth(getWidth(memento, "variance", 60));
		max.setWidth(getWidth(memento, "max", 60));
		path.setWidth(getWidth(memento, "path", 300));
		method.setWidth(getWidth(memento, "method", 150));
	}

	void updateWidths(IMemento memento) {
		memento.putInteger("description", description.getWidth());
		memento.putInteger("value", value.getWidth());
		memento.putInteger("average", average.getWidth());
		memento.putInteger("variance", variance.getWidth());
		memento.putInteger("max", max.getWidth());
		memento.putInteger("path", path.getWidth());
		memento.putInteger("method", method.getWidth());
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
		TreeItem item = (TreeItem) e.item;
		if (item.getData("source") != null) {
			AbstractMetricSource source = (AbstractMetricSource) item.getData("source");
			String metric = (String) item.getData("metric");
			String per = (String) item.getData("per");
			addChildren(item, source, metric, per);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
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

}
