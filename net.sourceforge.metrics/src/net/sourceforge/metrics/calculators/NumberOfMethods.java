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

import java.util.ArrayList;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.TypeMetrics;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.corext.refactoring.Checks;

/**
 * Counts the number of methods in a class. Distinguishes between statics and instance methods, sets NUM_METHODS and NUM_STAT_METHODS
 * 
 * @author Frank Sauer
 */
@SuppressWarnings("restriction")
public class NumberOfMethods extends Calculator implements Constants {

	/**
	 * Constructor for NumberOfMethods.
	 */
	public NumberOfMethods() {
		super(NUM_METHODS);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("NumberOfMethods is only applicable to types");
		}
		try {
			IType tipo = ((IType) source.getJavaElement());
			IMethod[] methods = tipo.getMethods();
			IField[] fields = tipo.getFields();
			ArrayList<IMethod> gettersAndSetters = new ArrayList<IMethod>();
			
			int stats = 0;
			int inst = 0;
			int publ = 0;
			int poly = 0;
			//inst - getters and setters
			int numNormalMethods=0;
			
			for(IField field:fields){
				IMethod getter = GetterSetterUtil.getGetter(field);
				IMethod setter = GetterSetterUtil.getSetter(field);
				if(getter!=null)
					gettersAndSetters.add(getter);
				if(setter!=null)
					gettersAndSetters.add(setter);
			}	
			
			for (IMethod method : methods) {
				//ASTNodeSearchUtil.getMethodDeclarationNode(method, source.getCompilationUnit());
				if ((method.getFlags() & Flags.AccStatic) != 0) 
					stats++;
				else {
					inst++;
					//'Normal' method counter (w/o Getters and Setters)
					if(!gettersAndSetters.contains(method))
						numNormalMethods++;
					
				}
				TypeMetrics tm =(TypeMetrics)source;				
				if(!tipo.isInterface() && isPolymorphic(method, tm.getHierarchy()))
					poly++;
				if((method.getFlags() & Flags.AccPublic) != 0)
					publ++;
			}
			source.setValue(new Metric(NUM_NORMAL_METHODS, numNormalMethods));
			source.setValue(new Metric(NUM_METHODS, inst));
			source.setValue(new Metric(NUM_STAT_METHODS, stats));
			source.setValue(new Metric(CIS, publ));
			source.setValue(new Metric(NOPM,poly));
		} catch (JavaModelException e) {
			source.setValue(new Metric(NUM_NORMAL_METHODS, 0));
			source.setValue(new Metric(NUM_METHODS, 0));
			source.setValue(new Metric(NUM_STAT_METHODS, 0));
			source.setValue(new Metric(CIS,0));
			source.setValue(new Metric(NOPM,0));
		}
	}
	
	private boolean isPolymorphic(IMethod method, ITypeHierarchy hierarchy) throws JavaModelException{
		for(IType subType:hierarchy.getAllSubtypes(hierarchy.getType())){
			IMethod found = Checks.findMethod(method, subType);
			if(found!=null)
				return true;
		}
		return false;		
	}

}
