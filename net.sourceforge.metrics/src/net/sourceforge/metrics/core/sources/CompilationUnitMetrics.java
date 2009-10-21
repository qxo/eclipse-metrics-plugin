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
 * $Id: CompilationUnitMetrics.java,v 1.41 2004/05/29 03:39:38 sauerf Exp $
 */

package net.sourceforge.metrics.core.sources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.internal.xml.IXMLExporter;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Calculates metrics for an ICompilationUnit
 * 
 * @author Frank Sauer
 */
public class CompilationUnitMetrics extends AbstractMetricSource {

	static final long serialVersionUID = 5392710341493985195L;

	transient private CompilationUnit astNode;

	public CompilationUnitMetrics() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sourceforge.metrics.core.sources.AbstractMetricSource# initializeNewInstance (net.sourceforge.metrics.core.sources.AbstractMetricSource, org.eclipse.jdt.core.IJavaElement)
	 */
	@Override
	public void initializeNewInstance(AbstractMetricSource newSource, IJavaElement element, Map data) {
		((TypeMetrics) newSource).setAstNode((ASTNode) data.get("type"));
		super.initializeNewInstance(newSource, element, data);
	}

	@Override
	protected void initializeChildren(AbstractMetricSource parentMetric) {
		ICompilationUnit unit = (ICompilationUnit) getJavaElement();
		try {
			unit.getUnderlyingResource().deleteMarkers("net.sourceforge.metrics.outofrangemarker", true, IResource.DEPTH_INFINITE);
		} catch (Throwable e) {
			Log.logError("Could not delete markers", e);
		}
		astNode = getAST();
		if (metricsInterruptus()) {
			return;
		}
		List types = astNode.types();
		int interfaces = 0;
		for (Iterator i = types.iterator(); i.hasNext();) {
			if (metricsInterruptus()) {
				return;
			}
			AbstractTypeDeclaration abstractType = (AbstractTypeDeclaration) i.next();
			boolean isInterface = false;
			if (abstractType instanceof TypeDeclaration) {
				TypeDeclaration lastType = (TypeDeclaration) abstractType;
				if (lastType.isInterface()) {
					isInterface = true;
				}
			}
			IType type = unit.getType(abstractType.getName().getIdentifier());
			Map<String, AbstractTypeDeclaration> data = new HashMap<String, AbstractTypeDeclaration>();
			data.put("type", abstractType);
			TypeMetrics tm = (TypeMetrics) Dispatcher.calculateAbstractMetricSource(type, this, data);
			addChild(tm);
			Metric subNumOfInterface = tm.getValue(NUM_INTERFACES);
			interfaces += subNumOfInterface == null ? 0 : subNumOfInterface.intValue();
			if (isInterface) {
				interfaces++;
			}
		}
		setValue(new Metric(NUM_INTERFACES, interfaces));
	}

	/**
	 * @see metrics.core.IMetric#calculate(org.eclipse.jdt.core.IJavaElement)
	 */
	@Override
	public void calculate() {
		setValue(new Metric(NUM_TYPES, getSize()));
		super.calculate();
	}

	private CompilationUnit getAST() {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource((ICompilationUnit) getJavaElement());
			return (CompilationUnit) parser.createAST(null);
			// return
			// AST.parseCompilationUnit((ICompilationUnit)getJavaElement(),
			// false);
		} catch (RuntimeException e) {
			Log.logError("No AST obtained!", e);
			// occurs when the compilation unit gets deleted at a bad time
			return null;
		}
	}

	/**
	 * Returns the astNode.
	 * 
	 * @return CompilationUnit
	 */
	@Override
	public ASTNode getASTNode() {
		if (astNode == null) {
			// happens after deserialization
			astNode = getAST();
		}
		return astNode;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getLevel()
	 */
	@Override
	public int getLevel() {
		return COMPILATIONUNIT;
	}

	/**
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getCalculators()
	 */
	@Override
	protected List getCalculators() {
		return MetricsPlugin.getDefault().getCalculators("compilationUnit");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.core.sources.AbstractMetricSource#getExporter()
	 */
	@Override
	public IXMLExporter getExporter() {
		return IXMLExporter.COMPILATIONUNIT_EXPORTER;
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
