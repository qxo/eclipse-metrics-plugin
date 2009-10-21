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
 * $Id: McCabe.java,v 1.14 2005/05/17 18:21:04 sauerf Exp $
 */
package net.sourceforge.metrics.calculators;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

/** 
 * Calculate McCabe Cyclomatic Complexity for a method. This counts the number
 * of if, while, for, case, do, catch, and ?: occurences plus one.
 * 
 * @author Frank Sauer
 */
public class McCabe extends Calculator implements Constants {

	/**
	 * Constructor for McCabe.
	 */
	public McCabe() {
		super(MCCABE);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != METHOD) throw new InvalidSourceException("McCabe only applicable to methods");
		MethodDeclaration astNode = (MethodDeclaration)source.getASTNode();
		if (astNode == null) source.setValue(getZero());
		Block body = astNode.getBody();
		if (body == null) source.setValue(getZero());
		String sourceCode = null;
		try {
			sourceCode = source.getCompilationUnit().getSource();
		} catch (JavaModelException e) {
			Log.logError("No sourcecode for " + source.getHandle(), e);
		}
		McCabeVisitor mcb = new McCabeVisitor(sourceCode);
		astNode.accept(mcb);
		source.setValue(new Metric(getName(), mcb.cyclomatic));
	}
	
	private class McCabeVisitor extends ASTVisitor {
		
		private int cyclomatic = 1;
		private String source;
		
		McCabeVisitor(String source) {
			this.source = source;
		}
		
		public boolean visit(CatchClause node) {
			cyclomatic++;
			return true;
		}
		public boolean visit(ConditionalExpression node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}
		public boolean visit(DoStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}
		public boolean visit(ForStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}
		public boolean visit(IfStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}
		public boolean visit(SwitchCase node) {
			if (!node.isDefault()) cyclomatic++;			
			return true;
		}
		public boolean visit(WhileStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}
		public boolean visit(ExpressionStatement exs) {
			inspectExpression(exs.getExpression());
			return false;
		}
		/**
		 * Count occurrences of && and || (conditional and or)
		 * Fix for BUG 740253
		 * @param ex
		 */
		private void inspectExpression(Expression ex) {
			if ((ex != null)&&(source != null)) {
				int start = ex.getStartPosition();
				int end = start + ex.getLength();
				String expression = source.substring(start, end);
				char[] chars = expression.toCharArray();
				for (int i = 0; i < chars.length-1; i++) {
					char next = chars[i];
					if ((next == '&' || next == '|')&&(next == chars[i+1])) {
						cyclomatic++;
					}
				}
			}
		}
	}
}
