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
package net.sourceforge.metrics.core.sources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.ICalculator;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Base class for all metric sources. Used by calculators to retrieve the AST, IJavaElement, etc. and to store their results.
 * 
 * @author Frank Sauer
 */
public abstract class AbstractMetricSource implements Constants, Serializable {

	static final long serialVersionUID = 3488676461898775539L;

	protected String handle = null;
	transient private AbstractMetricSource parent = null;
	transient private List<AbstractMetricSource> children = null;
	private Map<String, Metric> values = new HashMap<String, Metric>();
	private Map<String, Avg> averages = new HashMap<String, Avg>();
	private Map<String, Max> maxima = new HashMap<String, Max>();
	private List<String> childHandles = new ArrayList<String>();

	private boolean doRecurse = true;

	/**
	 * Constructor for AbstractMetricSOurce.
	 */
	public AbstractMetricSource() {
		super();
	}

	public void addChild(AbstractMetricSource child) {
		if (child != null) {
			String handle = child.getHandle();
			if (!childHandles.contains(handle)) {
				childHandles.add(handle);
			}
			if (!getChildren().contains(child)) {
				getChildren().add(child);
				child.parent = this;
			}
		}
	}

	public AbstractMetricSource getParent() {
		return parent;
	}

	/**
	 * return a list of the handle identifiers of all my direct children
	 * 
	 * @return
	 */
	public List<String> getChildHandles() {
		return childHandles;
	}

	/**
	 * Return all direct children having a value for the given metric
	 * 
	 * @param per
	 * @param metric
	 * @return
	 */
	public AbstractMetricSource[] getChildrenHaving(String per, String metric) {
		List<AbstractMetricSource> result = new ArrayList<AbstractMetricSource>();
		for (Object element : childHandles) {
			IJavaElement elm = JavaCore.create((String) element);
			if (elm != null) {
				AbstractMetricSource next = Dispatcher.getAbstractMetricSource(elm);
				if (next != null) {
					Metric val = next.getValue(metric);
					Max max = next.getMaximum(metric, per);
					Avg avg = next.getAverage(metric, per);
					if ((val != null) | (max != null) | (avg != null)) {
						result.add(next);
					}
				}
			}
		}
		return result.toArray(new AbstractMetricSource[] {});
	}

	public String getName() {
		return (getJavaElement() == null) ? "" : getJavaElement().getElementName();
	}

	public List<AbstractMetricSource> getChildren() {
		if (children == null) {
			children = new ArrayList<AbstractMetricSource>();
		}
		return children;
	}

	public int getSize() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}

	/**
	 * get the ICompilationUnit for the source
	 * 
	 * @return ICompilationUnit
	 */
	public ICompilationUnit getCompilationUnit() {
		IJavaElement input = getJavaElement();
		if (input.getElementType() == IJavaElement.COMPILATION_UNIT) {
			return (ICompilationUnit) input;
		} /* else { */
		return (ICompilationUnit) input.getAncestor(IJavaElement.COMPILATION_UNIT);
		/* } */
	}

	public CompilationUnit getParsedCompilationUnit() {
		ASTNode node = getASTNode();
		if (node == null) {
			return null;
		}
		return (CompilationUnit) node.getRoot();
	}

	/**
	 * @see metrics.core.IMetricSource#calculate(org.eclipse.jdt.core.IJavaElement)
	 */
	public void calculate() {
		// System.err.println("calculate: " +
		// getJavaElement().getHandleIdentifier());
		invokeCalculators();
	}

	/**
	 * Do not use
	 * 
	 * @see metrics.core.IMetricSource#setInputElement(org.eclipse.jdt.core.IJavaElement)
	 */
	public void setJavaElement(IJavaElement input) {
		this.handle = input.getHandleIdentifier();
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	protected abstract void initializeChildren(AbstractMetricSource parentMetric);

	public void initializeNewInstance(AbstractMetricSource newSource, IJavaElement element, Map<String, ? extends ASTNode> data) {
		newSource.childHandles.clear();
		newSource.setJavaElement(element);
	}

	/**
	 * Used by Calculators to store their result metrics. As a side effect, adds a marker of type net.sourceforge.metrics.outofrangemarker if enabled and the value is out of the range indicated by the metric's descriptor
	 * 
	 * @param value
	 */
	public void setValue(Metric value) {
		// System.err.println(input.getElementName()+"."+value.getName() + " = "
		// + value.doubleValue());
		values.put(value.getName(), value);
		if (MetricsPlugin.isWarningsEnabled() && !value.isPropagated()) {
			checkRange(value);
		}
	}

	/**
	 * @param value
	 */
	private void checkRange(Metric value) {
		MetricDescriptor md = MetricsPlugin.getDefault().getMetricDescriptor(value.getName());
		if (!md.isValueInRange(value.doubleValue())) {
			try {
				IResource resource = getJavaElement().getUnderlyingResource();
				if (resource != null) {
					configureOutOfRangeMarker(value, md, resource);
				}
			} catch (Throwable e) {
				Log.logError("could not get resource to add marker", e);
			}
		}
	}

	private void configureOutOfRangeMarker(Metric value, MetricDescriptor md, IResource resource) throws CoreException {
		IMarker marker = resource.createMarker("net.sourceforge.metrics.outofrangemarker");
		if ((marker != null) && (marker.exists())) {
			JavaCore.getJavaCore().configureJavaElementMarker(marker, getJavaElement());
			marker.setAttribute("metric", value.getName());
			marker.setAttribute("value", "" + value.doubleValue());
			String message = createMessage(value, md);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			if (getJavaElement() instanceof IMember) {
				IMember m = (IMember) getJavaElement();
				int offset = m.getNameRange().getOffset();
				int length = m.getNameRange().getLength();
				marker.setAttribute(IMarker.CHAR_START, offset);
				marker.setAttribute(IMarker.CHAR_END, offset + length);
				CompilationUnit cu = getParsedCompilationUnit();
				if (cu != null) {
					marker.setAttribute(IMarker.LINE_NUMBER, cu.lineNumber(offset));
				}
			}
		}
	}

	/**
	 * @param value
	 * @param md
	 * @return
	 */
	private String createMessage(Metric value, MetricDescriptor md) {
		StringBuffer b = new StringBuffer();
		b.append(md.getName()).append(" ").append(value.doubleValue());
		b.append(" is not in safe range [");
		String min = (md.getMin() == null) ? "0" : md.getMin().toString();
		String max = (md.getMax() == null) ? "0" : md.getMax().toString();
		b.append(min).append(" - ").append(max).append("]; ");
		b.append(md.getHint());
		return b.toString();
	}

	/**
	 * Used by Propagators to store their result averages
	 * 
	 * @param value
	 */
	public void setAverage(Avg value) {
		// System.err.println("AVG " + input.getElementName() +
		// "."+value.getName() + " per " + value.getPer() + " = " +
		// value.doubleValue());
		averages.put(value.getPer() + value.getName(), value);
	}

	/**
	 * Used by Propagators to store their result maxima
	 * 
	 * @param value
	 */
	public void setMaximum(Max value) {
		// System.err.println("MAX " + input.getElementName() +
		// "."+value.getName() + " per " + value.getPer() + " = " +
		// value.doubleValue());
		maxima.put(value.getPer() + value.getName(), value);
	}

	/**
	 * get the Metric with the given id
	 * 
	 * @param id
	 * @return Metric
	 */
	public Metric getValue(String id) {
		return values.get(id);
	}

	/**
	 * get all values (Metric objects)
	 * 
	 * @return Iterator
	 */
	public Map<String, Metric> getValues() {
		return values;
	}

	/**
	 * not meant for public use
	 */
	public void recurse(AbstractMetricSource parent) {
		if (doRecurse) {
			initializeChildren(parent);
			if (children != null) {
				for (Object element : children) {
					if (metricsInterruptus()) {
						return;
					}
					AbstractMetricSource next = (AbstractMetricSource) element;
					next.recurse(parent);
				}
			}
			calculate();
			save();
			doRecurse = false;
		}
	}

	protected void save() {
		detachChildren();
		Cache.singleton.put(this);
	}

	/**
	 * 
	 */
	private void detachChildren() {
		if (children == null) {
			return;
		}
		for (Iterator<AbstractMetricSource> i = children.iterator(); i.hasNext();) {
			AbstractMetricSource next = i.next();
			next.dispose();
			i.remove();
		}
		children = null;
	}

	protected void dispose() {
		parent = null;
	}

	/**
	 * Answers whether the current thread has been interrupted. Used to prematurely abort a long calculation
	 * 
	 * @return boolean
	 */
	protected boolean metricsInterruptus() {
		return Thread.currentThread().isInterrupted();
	}

	/**
	 * Get a list of all metrics defined in this node's children with the given name
	 * 
	 * @param name
	 * @return List
	 */
	public List<Metric> getMetricsFromChildren(String name) {
		List<Metric> metrics = new ArrayList<Metric>();
		for (Object element : getChildren()) {
			AbstractMetricSource next = (AbstractMetricSource) element;
			Metric m = next.getValue(name);
			if (m != null) {
				metrics.add(m);
			} else {
				Log.logMessage("metric " + name + " not found in " + next.getJavaElement().getElementName());
			}
		}
		return metrics;
	}

	/**
	 * calculate average of metrics defined in this node's children with the given name
	 * 
	 * @param name
	 * @param per
	 * @return List
	 */
	public List<Avg> getAveragesFromChildren(String name, String per) {
		List<Avg> averages = new ArrayList<Avg>();
		for (Object element : getChildren()) {
			AbstractMetricSource next = (AbstractMetricSource) element;
			Avg nextAvg = next.getAverage(name, per);
			if (nextAvg != null) {
				averages.add(nextAvg);
			} else {
				Log.logMessage("average " + name + "," + per + " not found in " + next.getJavaElement().getElementName());
			}
		}
		return averages;
	}

	/**
	 * calculate maximum of metrics defined in this node's children with the given name
	 * 
	 * @param name
	 * @param per
	 * @return List
	 */
	public List<Max> getMaximaFromChildren(String name, String per) {
		List<Max> maxes = new ArrayList<Max>();
		for (Object element : getChildren()) {
			AbstractMetricSource next = (AbstractMetricSource) element;
			Max nextMax = next.getMaximum(name, per);
			if (nextMax != null) {
				maxes.add(nextMax);
			}
		}
		return maxes;
	}

	/**
	 * get the already calculated and cached average
	 * 
	 * @param name
	 * @param per
	 * @return Avg
	 */
	public Avg getAverage(String name, String per) {
		return averages.get(per + name);
	}

	public Map<String, Avg> getAverages() {
		return averages;
	}

	/**
	 * get the already calculated and cached maximum
	 * 
	 * @param name
	 * @param per
	 * @return Avg
	 */
	public Max getMaximum(String name, String per) {
		return maxima.get(per + name);
	}

	public Map<String, Max> getMaxima() {
		return maxima;
	}

	/**
	 * Get the IJavaElement of this node. Used frequently by calculators
	 * 
	 * @return IJavaElement
	 */
	public IJavaElement getJavaElement() {
		return JavaCore.create(handle);
	}

	/**
	 * returns the path if this object represent a compilation unit or bigger scope, null otherwise
	 * 
	 * @return
	 */
	public String getPath() {
		IJavaElement element = getJavaElement();
		if (element.getElementType() <= IJavaElement.COMPILATION_UNIT) {
			return element.getPath().toString();
		} /* else { */
		return null;
		/* } */
	}

	/**
	 * Get the AST of this node. Used frequently by calculators. This base class returns null. Implemented by CompilationUnitMetrics
	 * 
	 * @return IJavaElement
	 * @see net.sourceforge.metrics.sources.CompilationUnitMetrics#getASTNode()
	 */
	public abstract ASTNode getASTNode();/* {
		return null;
	}*/

	/**
	 * get the original compilation unit for the given IJavaElement. If the compilation unit turns out to be a WorkingCopy this method returns its original source compilation unit
	 * 
	 * @param input
	 * @return ICompilationUnit
	 */
	public static ICompilationUnit getOriginalCompilationUnit(IJavaElement input) {

		return (ICompilationUnit) input.getAncestor(IJavaElement.COMPILATION_UNIT);
	}

	public abstract int getLevel();

	protected List<ICalculator> getCalculators() {
		return new ArrayList<ICalculator>();
	}

	/**
	 * invokes calculate() on all calculators. TODO fine-grained Cache lookup so we can add new metrics
	 */
	protected void invokeCalculators() {
		for (Iterator<ICalculator> i = getCalculators().iterator(); i.hasNext();) {
			if (metricsInterruptus()) {
				return;
			}
			ICalculator c = i.next();
			try {
				c.calculate(this);
			} catch (OutOfMemoryError m) {
				throw m;
			} catch (Throwable e) {
				Log.logError("Error running calculators for " + getJavaElement().getHandleIdentifier(), e);
			}
		}
	}

	/**
	 * Sets the doRecurse.
	 * 
	 * @param doRecurse
	 *            The doRecurse to set
	 */
	public void setDoRecurse(boolean doRecurse) {
		this.doRecurse = doRecurse;
	}

	/**
	 * @return
	 */
	public String getHandle() {
		return handle;
	}

	/**
	 * @param store
	 * @param list
	 */
	// public void removed(Store store, ArrayList list) {
	// }

	/**
	 * @return
	 */
	public abstract IXMLExporter getExporter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o.getClass().equals(this.getClass())) {
			AbstractMetricSource s = (AbstractMetricSource) o;
			return s.getHandle().equals(getHandle());
		} /* else { */
		return false;
		/* } */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getHandle().hashCode();
	}

}
