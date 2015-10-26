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
package net.sourceforge.metrics.calculators.qmood;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.metrics.calculators.Calculator;
import net.sourceforge.metrics.calculators.InvalidSourceException;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsUtils;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * A count of the different number of classes that a class is directly related to. 
 * The metric includes classes that are directly related by attribute declaration and message passing
 * (parameters) in methods. Interpreted as an average over all classes when applied to a design as 
 * a whole; a count of the number of distinct user-defined classes a class is coupled to by method 
 * parameter or attribute type. *Definição de Cinnéide* 
 * 
 * @author Leonardo Jr.
 */
public class DirectClassCoupling extends Calculator implements Constants {

	/**
	 * Constructor for DCC.
	 */
	public DirectClassCoupling() {
		super(DCC);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("DirectClassCoupling is only applicable to types");
		}
		Set<String> tipos = new HashSet<String>();
		if(source.getASTNode() instanceof TypeDeclaration){
			TypeDeclaration type =  (TypeDeclaration)source.getASTNode();
			FieldDeclaration[] fields = type.getFields();
			MethodDeclaration[] methods = type.getMethods();		
			for (FieldDeclaration field : fields) 
				tipos.addAll(MetricsUtils.getSourceTypeNames(field.getType().resolveBinding()));					
			for (MethodDeclaration method : methods) 
				for (ITypeBinding binding : method.resolveBinding().getParameterTypes()) 
							tipos.addAll(MetricsUtils.getSourceTypeNames(binding));
					
		}
		source.setValue(new Metric(DCC,tipos.size()));
	}
}
