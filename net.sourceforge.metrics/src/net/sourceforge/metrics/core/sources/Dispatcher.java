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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.metrics.core.Log;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Based on the current selection figure out what metric source to create and instruct to calculate metrics, or simply pick up from the cache and use it.
 * 
 * @author Frank Sauer
 */
public class Dispatcher {

	protected static Dispatcher singleton = new Dispatcher();

	private Map<Class<? extends IJavaElement >, Class<? extends AbstractMetricSource>> sourcemap = null;

	protected Map<Class<? extends IJavaElement >, Class<? extends AbstractMetricSource>> getSourceMap() {
		if (sourcemap == null) {
			sourcemap = new HashMap<Class<? extends IJavaElement >, Class<? extends AbstractMetricSource>>();
			initMetrics();
		}
		return sourcemap;
	}

	/**
	 * initialize the IJavaElement type to AbstractMetricSource class map
	 */
	private void initMetrics() {
		sourcemap.put(IMethod.class, MethodMetrics.class);
		sourcemap.put(IType.class, TypeMetrics.class);
		sourcemap.put(IPackageFragment.class, PackageFragmentMetrics.class);
		sourcemap.put(IPackageFragmentRoot.class, PackageFragmentRootMetrics.class);
		sourcemap.put(ICompilationUnit.class, CompilationUnitMetrics.class);
		sourcemap.put(IJavaProject.class, ProjectMetrics.class);
	}

	/**
	 * Create a new AbstractMetricSource subclass instance appropriate for the given IJavaElement
	 * 
	 * @param input
	 * @return AbstractMetricSource
	 */
	protected AbstractMetricSource createNewSource(IJavaElement input) {
		Map<Class<? extends IJavaElement >, Class<? extends AbstractMetricSource>> metrics = getSourceMap();
		for (Iterator<Entry<Class<? extends IJavaElement>, Class<? extends AbstractMetricSource>>> entryIterator = metrics.entrySet().iterator(); entryIterator.hasNext();) {
			Entry<Class<? extends IJavaElement>, Class<? extends AbstractMetricSource>> entry = entryIterator.next();
			Class<? extends IJavaElement> key = entry.getKey();
			if (key.isInstance(input)) {
				try {
					Class<? extends AbstractMetricSource> msc = entry.getValue();
					AbstractMetricSource ms = msc.newInstance();
					return ms;
				} catch (InstantiationException e) {
					Log.logError("createNewSource for " + input.getHandleIdentifier(), e);
				} catch (IllegalAccessException e) {
					Log.logError("createNewSource for " + input.getHandleIdentifier(), e);
				}
			}
		}
		return null;
	}

	/**
	 * Get the AbstractMetricSource for the given IJavaElement from cache or create a new one and have it calculate the metrics.
	 * 
	 * @param input
	 * @return AbstractMetricSource
	 */
	public static AbstractMetricSource calculateAbstractMetricSource(IJavaElement input) {
		AbstractMetricSource m = getAbstractMetricSource(input);
		if (m == null) {
			IJavaElement calculate = input;
			// calculate from COMPILATION_UNIT down if type or method
			if (input.getElementType() > IJavaElement.COMPILATION_UNIT) {
				calculate = input.getAncestor(IJavaElement.COMPILATION_UNIT);
			}
			m = singleton.createNewSource(calculate);
			m.setJavaElement(calculate);
			m.recurse(null);
			// should be in cache now
			m = Cache.singleton.get(input);
		}
		return m;
	}

	/**
	 * Get the AbstractMetricSource for the given IJavaElement from cache or create a new one and have it calculate the metrics. This method will give the parent AbstractMetricSource a chance to initialize the new element (in cas e anew one
	 * is created) with the given data.
	 * 
	 * @param input
	 * @param parent
	 *            AbstractMetricSource with need to participate in creation process
	 * @param data
	 *            Map with additional data needed for creation
	 * @return AbstractMetricSource
	 * @see AbstractMetricSource#initializeNewInstance(AbstractMetricSource,IJavaElement,Map)
	 */
	public static AbstractMetricSource calculateAbstractMetricSource(IJavaElement input, AbstractMetricSource parent, Map<String, ? extends ASTNode> data) {
		AbstractMetricSource m = getAbstractMetricSource(input);
		if (m == null) {
			m = singleton.createNewSource(input);
			parent.initializeNewInstance(m, input, data);
			m.recurse(parent);
		} else {
			// happens if a previous remove from cache failed, simply
			// reinitialize existing instance
			// for recalculation
			parent.initializeNewInstance(m, input, data);
			Log.logMessage("Reusing instance for " + m.getHandle());
		}
		return m;
	}

	public static AbstractMetricSource getAbstractMetricSource(IJavaElement input) {
		AbstractMetricSource m = Cache.singleton.get(input);
		return m;
	}
}
