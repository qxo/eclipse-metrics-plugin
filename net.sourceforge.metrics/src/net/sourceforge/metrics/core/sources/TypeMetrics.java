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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.ICalculator;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Collect the result of all type calculators and setup the method sources
 * 
 * @author Frank Sauer
 */
public class TypeMetrics extends AbstractMetricSource {

	static final long serialVersionUID = -3250149247535864680L;

	transient private ASTNode astNode = null; // either a
	// AbstractTypeDeclaration or a
	// AnonymousClassDeclaration
	transient private ITypeHierarchy hierarchy = null;

	public TypeMetrics() {
		super();
	}

	public TypeMetrics(ASTNode t) {
		if (t == null) {
			throw new IllegalArgumentException("Must have a valid TypeDeclaration!");
		}
		astNode = t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sourceforge.metrics.core.sources.AbstractMetricSource# initializeNewInstance (net.sourceforge.metrics.core.sources.AbstractMetricSource, org.eclipse.jdt.core.IJavaElement)
	 */
	@Override
	public void initializeNewInstance(AbstractMetricSource newSource, IJavaElement element, Map<String, ? extends ASTNode> data) {
		if (newSource instanceof MethodMetrics) {
			((MethodMetrics) newSource).setAstNode((MethodDeclaration) data.get("method"));
		} else if (newSource instanceof TypeMetrics) {
			((TypeMetrics) newSource).setAstNode(data.get("type"));
		}
		super.initializeNewInstance(newSource, element, data);
	}

	@Override
	protected void initializeChildren(AbstractMetricSource parentMetric) {
		initializeTypeChildren(astNode, parentMetric, this, (IType) getJavaElement());
	}

	protected static void initializeTypeChildren(ASTNode astNode, AbstractMetricSource parentMetric, TypeMetrics a_TypeMetrics, IType enclosingType) {
		initializeTypeChildrenMethodPart(astNode, parentMetric, a_TypeMetrics, enclosingType);
		initializeTypeChildrenTypePart(astNode, parentMetric, a_TypeMetrics, enclosingType);
	}

	protected static void initializeTypeChildrenTypePart(ASTNode astNode, AbstractMetricSource parentMetric, TypeMetrics a_TypeMetrics, IType enclosingType) {
		ASTNode[] subTypes = findSubTypes(astNode);
		if (subTypes != null) {
			int interfaces = 0;
			for (ASTNode node : subTypes) {
				if (a_TypeMetrics.metricsInterruptus()) {
					return;
				}
				IType type;
				try {
					type = (IType) a_TypeMetrics.getCompilationUnit().getElementAt(node.getStartPosition());
					Map<String, ASTNode> data = new HashMap<String, ASTNode>();
					data.put("type", node);
					TypeMetrics tm = (TypeMetrics) Dispatcher.calculateAbstractMetricSource(type, parentMetric, data);
					parentMetric.addChild(tm);
					Metric subNumOfInterface = tm.getValue(NUM_INTERFACES);
					interfaces += subNumOfInterface == null ? 0 : subNumOfInterface.intValue();
					if (type.isInterface()) {
						interfaces++;
					}
				} catch (JavaModelException e) {
					Log.logError("Could not get IJavaElement hierarchy for " + node, e);
				}
			}
			a_TypeMetrics.setValue(new Metric(NUM_INTERFACES,interfaces));
		}
	}

	protected static void initializeTypeChildrenMethodPart(ASTNode astNode, AbstractMetricSource parentMetric, TypeMetrics a_TypeMetrics, IType enclosingType) {
		MethodDeclaration[] methods = null;
		if (astNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclarationNode = (TypeDeclaration) astNode;
			methods = getMethods(typeDeclarationNode);
		} else if (astNode instanceof EnumDeclaration) {
			EnumDeclaration enumDeclaration = (EnumDeclaration) astNode;
			methods = getMethods(enumDeclaration);
		} else if (astNode instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) astNode;
			methods = getMethods(anonymousClassDeclaration);
		}
		if (methods != null) {
			for (MethodDeclaration lastMethod : methods) {
				if (a_TypeMetrics.metricsInterruptus()) {
					return;
				}
				IMethod method = findMethod(lastMethod, enclosingType);
				Map<String, ASTNode> data = new HashMap<String, ASTNode>();
				data.put("method", lastMethod);
				MethodMetrics mm = (MethodMetrics) Dispatcher.calculateAbstractMetricSource(method, a_TypeMetrics, data);
				a_TypeMetrics.addChild(mm);
			}
		}
	}

	private static ASTNode[] findSubTypes(final ASTNode startpoint) {
		ASTNode[] subTypes = null;
		final Set<ASTNode> subTypeList = new LinkedHashSet<ASTNode>();
		ASTVisitor subTypesFinder = new ASTVisitor() {
			@Override
			public boolean visit(TypeDeclaration node) {
				return addASTNode(node);
			}

			@Override
			public boolean visit(AnnotationTypeDeclaration node) {
				return addASTNode(node);
			}

			@Override
			public boolean visit(EnumDeclaration node) {
				return addASTNode(node);
			}

			@Override
			public boolean visit(AnonymousClassDeclaration node) {
				return addASTNode(node);
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				return false; // allready processed using methods.
			}

			private boolean addASTNode(ASTNode node) {
				boolean l_return = false;
				if (node == startpoint) {
					l_return = true;
				} else {
					subTypeList.add(node); // XXX do not recurse it. it will be
					// done while processing this
					// subType
				}
				return l_return;

			}
		};
		startpoint.accept(subTypesFinder);
		if (!subTypeList.isEmpty()) {
			subTypes = subTypeList.toArray(new ASTNode[1]);
		}
		return subTypes;
	}

	private static MethodDeclaration[] getMethods(AnonymousClassDeclaration anonymousClassDeclaration) {
		List<?> bd = anonymousClassDeclaration.bodyDeclarations();
		int methodCount = 0;
		for (Iterator<?> it = bd.listIterator(); it.hasNext();) {
			if (it.next() instanceof MethodDeclaration) {
				methodCount++;
			}
		}
		MethodDeclaration[] methods = new MethodDeclaration[methodCount];
		int next = 0;
		for (Iterator<?> it = bd.listIterator(); it.hasNext();) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methods[next++] = (MethodDeclaration) decl;
			}
		}
		return methods;
	}

	private static MethodDeclaration[] getMethods(EnumDeclaration enumDeclaration) {
		List<?> bd = enumDeclaration.bodyDeclarations();
		int methodCount = 0;
		for (Iterator<?> it = bd.listIterator(); it.hasNext();) {
			if (it.next() instanceof MethodDeclaration) {
				methodCount++;
			}
		}
		MethodDeclaration[] methods = new MethodDeclaration[methodCount];
		int next = 0;
		for (Iterator<?> it = bd.listIterator(); it.hasNext();) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methods[next++] = (MethodDeclaration) decl;
			}
		}
		return methods;
	}

	private static MethodDeclaration[] getMethods(TypeDeclaration typeDeclarationNode) {
		return typeDeclarationNode.getMethods();
	}

	private static IMethod findMethod(MethodDeclaration m, IType type) {
		List<?> parms = m.parameters();
		String[] argtypes = new String[parms.size()];
		int index = 0;
		for (Iterator<?> i = parms.iterator(); i.hasNext(); index++) {
			SingleVariableDeclaration l_varDeclaration = (SingleVariableDeclaration) i.next();
			String svd = l_varDeclaration.getType().toString();
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
	@Override
	public ASTNode getASTNode() {
		return astNode;
	}

	/**
	 * Returns the hierarchy.
	 * 
	 * @return ITypeHierarchy
	 */
	public ITypeHierarchy getHierarchy() {
		if (hierarchy == null) {
			IType iType = (IType) getJavaElement();
			try {
				hierarchy = iType.newTypeHierarchy((IJavaProject) iType.getAncestor(IJavaElement.JAVA_PROJECT), null);
			} catch (Throwable e) {
				Log.logError("Could not get type hierarchy for " + getHandle(), e);
			}
		}
		return hierarchy;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	@Override
	public int getLevel() {
		return TYPE;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	@Override
	protected List<ICalculator> getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("type");
	}

	/**
	 * Sets the astNode.
	 * 
	 * @param astNode
	 *            The astNode to set
	 */
	public void setAstNode(ASTNode astNode) {
		this.astNode = astNode;
	}

	/**
	 * Sets the hierarchy.
	 * 
	 * @param hierarchy
	 *            The hierarchy to set
	 */
	public void setHierarchy(ITypeHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	@Override
	public IXMLExporter getExporter() {
		return IXMLExporter.TYPE_EXPORTER;
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
		hierarchy = null;
	}

}
