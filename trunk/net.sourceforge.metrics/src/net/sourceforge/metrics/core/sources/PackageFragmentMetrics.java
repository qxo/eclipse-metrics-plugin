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

import java.util.List;
import java.util.Set;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;


/**
 * initialize package fragment metrics (my children) and collect all results
 * from the calculators for the package level
 * @author Frank Sauer
 */
public class PackageFragmentMetrics extends AbstractMetricSource {
	
	static final long serialVersionUID = 552400421568822970L;	

	private Set efferent;

	public PackageFragmentMetrics() {
		super();
	}
	
	protected void initializeChildren(AbstractMetricSource parentMetric) {
		IPackageFragment pack = (IPackageFragment)getJavaElement();
		try {
			IJavaElement[] children = pack.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof ICompilationUnit) {
					// bug 737542
					AbstractMetricSource next = Dispatcher.getAbstractMetricSource( children[i]);
					if (next != null) addChild(next);
					else Log.logError("Can't initialize AbstractMetricSource for " + children[i].getElementName(), null);
				}
			}
		} catch (JavaModelException e) {
			Log.logError("PackageFragmentMetrics.initializeChildren", e);
		}	
	}
	
	public void recurse(AbstractMetricSource parentMetric) {
		initializeChildren(parentMetric);
		calculate();
		save();
	}
		
	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	public int getLevel() {
		return PACKAGEFRAGMENT;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	protected List getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("packageFragment");
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	public IXMLExporter getExporter() {
		return IXMLExporter.PACKAGEFRAGMENT_EXPORTER;
	}

	/**
	 * @param packages
	 */
	public void setEfferentDependencies(Set packages) {
		this.efferent = packages;
	}

	/**
	 * @return
	 */
	public Set getEfferentDependencies() {
		return efferent;
	}

}
