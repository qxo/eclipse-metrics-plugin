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
package net.sourceforge.metrics.calculators;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Calculate max nested block depth for a method
 * 
 * @author Frank Sauer
 */
public class NestedBlockDepth extends Calculator implements Constants {

	/**
	 * Constructor for NestedBlockDepth.
	 */
	public NestedBlockDepth() {
		super(NESTEDBLOCKDEPTH);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != METHOD) throw new InvalidSourceException("NestedBlockDepth only applicable to methods");
		MethodDeclaration astNode = (MethodDeclaration)source.getASTNode();
		if (astNode == null) source.setValue(getZero());
		Block body = astNode.getBody();
		if (body == null) source.setValue(getZero());
		LevelCounter lc = new LevelCounter();
		astNode.accept(lc);
		source.setValue(new Metric(getName(), lc.maxDepth));
	}
	
	/**
	 * counts the maximum block depth by visiting Blocks
	 */
	private class LevelCounter extends ASTVisitor {
		
		int maxDepth = 0;
		int depth = 0;
				
		public boolean visit(Block node) {
			depth++;
			return true;
		}
		
		public void endVisit(Block node) {
			if (depth > maxDepth) {
				maxDepth = depth;
			}
			depth--;
		}

	}
}
