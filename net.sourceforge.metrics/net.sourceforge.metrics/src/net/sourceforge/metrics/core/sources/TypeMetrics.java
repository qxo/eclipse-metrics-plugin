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
import java.util.List;
import java.util.Map;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Collect the result of all type calculators and setup the method sources
 *
 * @author Frank Sauer
 */
public class TypeMetrics extends AbstractMetricSource {

	static final long serialVersionUID = -3250149247535864680L;	
	
	transient private TypeDeclaration astNode = null;
	transient private ITypeHierarchy hierarchy = null;
		
	public TypeMetrics() {
		super();
	}

	public TypeMetrics(TypeDeclaration t) {
		if (t == null) throw new IllegalArgumentException("Must have a valid TypeDeclaration!");
		astNode = t;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#initializeNewInstance(net.sourceforge.metrics.core.sources.AbstractMetricSource, org.eclipse.jdt.core.IJavaElement)
	 */
	public void initializeNewInstance(AbstractMetricSource newSource, IJavaElement element, Map data) {
		((MethodMetrics)newSource).setAstNode((MethodDeclaration)data.get("method"));
		super.initializeNewInstance(newSource, element, data);
	}	
	
	protected void initializeChildren() {
		MethodDeclaration[] methods = astNode.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (metricsInterruptus()) return;
			MethodDeclaration lastMethod = methods[i];
			IMethod method = findMethod(lastMethod);
			Map data = new HashMap();
			data.put("method", lastMethod);
			MethodMetrics mm = (MethodMetrics) Dispatcher.calculateAbstractMetricSource(method, this, data);
			addChild(mm);
		}
	}
	
	private IMethod findMethod(MethodDeclaration m) {
		IType type = (IType)getJavaElement();
		List parms = m.parameters();
		String[] argtypes = new String[parms.size()];
		int index = 0;
		for (Iterator i = parms.iterator();i.hasNext();index++) {
			String svd = i.next().toString();
			int space = svd.lastIndexOf(' ');
			// FIX submitted by Jacob Eckel 5/27/03 - remove final modifiers
			svd = svd.substring(0, space).trim();
			if (svd.startsWith("final")) 
				svd = svd.substring("final".length()).trim();
			// FIX F.S. 2/23/03 - without, invalid handles are produced by found method
			argtypes[index] = Signature.createTypeSignature(svd, false);
		}
		IMethod im = type.getMethod(m.getName().getIdentifier(), argtypes);
		if (im == null) {
			Log.logError("No method found for " + m.getName().getIdentifier() + " (" + argtypes + ")", null);
		} 
		return im;
	}
	
	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getASTNode()
	 */
	public ASTNode getASTNode() {
		return astNode;
	}

	/**
	 * Returns the hierarchy.
	 * @return ITypeHierarchy
	 */
	public ITypeHierarchy getHierarchy() {
		if (hierarchy == null) {
			IType iType = (IType)getJavaElement();
			try {
				hierarchy = iType.newTypeHierarchy((IJavaProject)iType.getAncestor(IJavaElement.JAVA_PROJECT),null);
			} catch (Throwable e) {
				Log.logError("Could not get type hierarchy for " + getHandle(), e);
			}
		}
		return hierarchy;
	}
	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	public int getLevel() {
		return TYPE;
	}
	
	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	protected List getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("type");
	}
	
	/**
	 * Sets the astNode.
	 * @param astNode The astNode to set
	 */
	public void setAstNode(TypeDeclaration astNode) {
		this.astNode = astNode;
	}

	/**
	 * Sets the hierarchy.
	 * @param hierarchy The hierarchy to set
	 */
	public void setHierarchy(ITypeHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	public IXMLExporter getExporter() {
		return IXMLExporter.TYPE_EXPORTER;
	}
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#dispose()
	 */
	protected void dispose() {
		super.dispose();
		astNode = null;
		hierarchy = null;
	}

}
