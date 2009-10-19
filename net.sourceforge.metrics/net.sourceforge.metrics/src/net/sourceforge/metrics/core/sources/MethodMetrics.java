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

import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Collect the results of all Method calculators for each method
 *
 * @author Frank Sauer 
 */
public class MethodMetrics extends AbstractMetricSource {

	static final long serialVersionUID = -1060277695642364018L;	
	
	transient private MethodDeclaration astNode = null;

	public MethodMetrics() {
		super();
	}
	
	
	protected void initializeChildren() {
	}
	
	public MethodMetrics(MethodDeclaration methodDeclaration) {
		astNode = methodDeclaration;
		if (astNode == null) {
			System.err.println("MethodMetrics: Must provide legal MethodDeclaration!");
			throw new IllegalArgumentException("MethodMetrics: Must provide legal MethodDeclaration!");
		}
	}

	public String getMethodName() {
		return getJavaElement().getElementName();
	}
	
	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getASTNode()
	 */
	public ASTNode getASTNode() {
		return astNode;
	}
	
	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	public int getLevel() {
		return METHOD;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	protected List getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("method");
	}

	/**
	 * Sets the astNode.
	 * @param astNode The astNode to set
	 */
	public void setAstNode(MethodDeclaration astNode) {
		this.astNode = astNode;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	public IXMLExporter getExporter() {
		return IXMLExporter.METHOD_EXPORTER;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#dispose()
	 */
	protected void dispose() {
		super.dispose();
		astNode = null;
	}

}
