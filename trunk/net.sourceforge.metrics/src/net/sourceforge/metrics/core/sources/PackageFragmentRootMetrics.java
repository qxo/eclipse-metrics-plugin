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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.ICalculator;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * initialize package fragment metrics (my children) and collect all results from the calculators for the package root level
 * 
 * @author Frank Sauer
 */
public class PackageFragmentRootMetrics extends AbstractMetricSource implements IGraphContributor {

	static final long serialVersionUID = -2993178574625592263L;

	private Map<String, Set<String>> efferent;

	public PackageFragmentRootMetrics() {
		super();
	}

	@Override
	protected void initializeChildren(AbstractMetricSource parentMetric) {
		IPackageFragmentRoot pack = (IPackageFragmentRoot) getJavaElement();
		try {
			IJavaElement[] children = pack.getChildren();
			for (IJavaElement element : children) {
				if (element instanceof IPackageFragment) {
					if (((IPackageFragment) element).containsJavaResources()) {
						AbstractMetricSource next = Dispatcher.getAbstractMetricSource(element);
						if (next != null) {
							addChild(next);
						} else {
							Log.logMessage("metrics for package " + element.getHandleIdentifier() + " not found.");
						}
					}
				}
			}
		} catch (JavaModelException e) {
			Log.logError("PackageFragmentRoot.initializeChildren:", e);
		}

	}

	@Override
	public void recurse(AbstractMetricSource parentMetric) {
		initializeChildren(parentMetric);
		calculate();
		save();
	}

	/**
	 * @see metrics.core.IMetric#calculate()
	 */
	@Override
	public void calculate() {
		setValue(new Metric(NUM_PACKAGES, getSize()));
		super.calculate();
		createDependencyGraph();
	}

	/**
	 * 
	 */
	private void createDependencyGraph() {
		efferent = new HashMap<String, Set<String>>();
		for (Object element : getChildren()) {
			PackageFragmentMetrics next = (PackageFragmentMetrics) element;
			Set<String> deps = next.getEfferentDependencies();
			String name = next.getName();
			efferent.put(name, deps);
		}
		// displayDependencyGraphSWT();
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	@Override
	public int getLevel() {
		return PACKAGEROOT;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	@Override
	protected List<ICalculator> getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("packageFragmentRoot");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	@Override
	public IXMLExporter getExporter() {
		return IXMLExporter.PACKAGEROOT_EXPORTER;
	}

	/**
	 * @return
	 */
	public Map<String, Set<String>> getEfferent() {
		return efferent;
	}

	@Override
	public ASTNode getASTNode() {
		return null;
	}

}
