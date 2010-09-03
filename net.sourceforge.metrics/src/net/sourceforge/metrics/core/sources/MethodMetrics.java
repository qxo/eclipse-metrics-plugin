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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.ICalculator;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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

	@Override
	public void initializeNewInstance(AbstractMetricSource newSource, IJavaElement element, Map<String, ? extends ASTNode> data) {
		((TypeMetrics) newSource).setAstNode(data.get("type"));
		super.initializeNewInstance(newSource, element, data);
	}

	@Override
	protected void initializeChildren(AbstractMetricSource parentMetric) {
		ASTNode[] subTypes = findSubTypes();
		if (subTypes != null) {
			for (ASTNode lastSubType : subTypes) {
				if (metricsInterruptus()) {
					return;
				}
				boolean isInterface = false;
				if (lastSubType instanceof TypeDeclaration) {
					TypeDeclaration lastType = (TypeDeclaration) lastSubType;
					if (lastType.isInterface()) {
						isInterface = true;
					}
				}
				if (!isInterface) {
					IType type;
					try {
						type = (IType) getCompilationUnit().getElementAt(lastSubType.getStartPosition());
						Map<String, ASTNode> data = new HashMap<String, ASTNode>();
						data.put("type", lastSubType);
						TypeMetrics tm = (TypeMetrics) Dispatcher.calculateAbstractMetricSource(type, this, data);
						addChild(tm);
					} catch (JavaModelException e) {
						Log.logError("Could not get IJavaElement hierarchy for " + lastSubType, e);
					}
				}
			}
		}
	}

	private ASTNode[] findSubTypes() {
		ASTNode[] subTypes = null;
		final Set<ASTNode> subTypeList = new LinkedHashSet<ASTNode>();
		ASTVisitor subTypesFinder = new ASTVisitor() {
			@Override
			public boolean visit(TypeDeclaration node) {
				return AddNode(node);
			}

			@Override
			public boolean visit(AnnotationTypeDeclaration node) {
				return AddNode(node);
			}

			@Override
			public boolean visit(EnumDeclaration node) {
				return AddNode(node);
			}

			@Override
			public boolean visit(AnonymousClassDeclaration node) {
				return AddNode(node);
			}

			private boolean AddNode(ASTNode node) {
				subTypeList.add(node);
				return false;// XXX do not recurse it will be done while
				// processing this subType

			}
		};
		astNode.accept(subTypesFinder);
		if (!subTypeList.isEmpty()) {
			subTypes = subTypeList.toArray(new ASTNode[1]);
		}
		return subTypes;
	}

	public MethodMetrics(MethodDeclaration methodDeclaration) {
		astNode = methodDeclaration;
		if (astNode == null) {
			throw new IllegalArgumentException("MethodMetrics: Must provide legal MethodDeclaration!");
		}
	}

	public String getMethodName() {
		return getJavaElement().getElementName();
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getASTNode()
	 */
	@Override
	public ASTNode getASTNode() {
		return astNode;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	@Override
	public int getLevel() {
		return METHOD;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	@Override
	protected List<ICalculator> getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("method");
	}

	/**
	 * Sets the astNode.
	 * 
	 * @param astNode
	 *            The astNode to set
	 */
	public void setAstNode(MethodDeclaration astNode) {
		this.astNode = astNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	@Override
	public IXMLExporter getExporter() {
		return IXMLExporter.METHOD_EXPORTER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#dispose()
	 */
	@Override
	protected void dispose() {
		super.dispose();
		astNode = null;
	}

}
