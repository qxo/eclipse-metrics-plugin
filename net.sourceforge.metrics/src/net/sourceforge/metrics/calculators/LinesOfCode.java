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

import java.util.StringTokenizer;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Calculate number of real lines of code in a method. Does not count
 * blank lines and comment lines.
 * 
 * @author Frank Sauer
 * @deprecated Use AbstractLinesOfCode instead (and MethodLinesOfCode / TotalLinesOfCode)
 */
public class LinesOfCode extends Calculator implements Constants {

	/**
	 * Constructor for Lines.
	 * @param name
	 */
	public LinesOfCode() {
		super(LINES);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != METHOD) throw new InvalidSourceException("LinesOfCode only applicable to methods");
		MethodDeclaration astNode = (MethodDeclaration)source.getASTNode();
		if (astNode != null) {
			String text = getSource(astNode, source);
			source.setValue(new Metric(name, calculateLines(text)));
		} else {
			System.err.println("Error in LinesOfCode: no MethodDeclaration");
		}
	}

	public String getSource(MethodDeclaration astNode, AbstractMetricSource mm) {
		int start = astNode.getStartPosition();
		int length = astNode.getLength();
		
		ICompilationUnit unit = mm.getCompilationUnit();
		try {
			String source = unit.getSource();
			return source.substring(start, start + length);
		} catch (JavaModelException e) {
			Log.logError("LinesOfCode:getSource", e);
			return null;
		}		
	}
		
	private int calculateLines(String source) {
		int total = 0;
		int firstCurly = source.indexOf('{');
		if (firstCurly != -1) {
			String body = source.substring(firstCurly+1, source.length()-1).trim();		
			StringTokenizer lines = new StringTokenizer(body, "\n");
			while(lines.hasMoreTokens()) {
				String trimmed = lines.nextToken().trim();
				if (trimmed.length() == 0) continue;
				if (trimmed.startsWith("/*")) {
					while (trimmed.indexOf("*/") == -1) {
						trimmed = lines.nextToken().trim();
					}
					if (lines.hasMoreTokens()) trimmed = lines.nextToken().trim();
				}
				if (!trimmed.startsWith("//")) total++;
			
			}
		} 					
		return total;
	}

}