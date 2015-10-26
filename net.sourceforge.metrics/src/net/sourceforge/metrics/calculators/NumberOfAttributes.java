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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsUtils;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Counts the number of attributes in a class. Distinguishes between statics and instance fields, sets NUM_FIELDS and NUM_STAT_FIELDS
 * 
 * @author Frank Sauer
 */
public class NumberOfAttributes extends Calculator implements Constants {

	/**
	 * Constructor for NumberOfAttributes.
	 */
	public NumberOfAttributes() {
		super(NUM_FIELDS);
	}
	
	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("NumberOfAttributes is only applicable to types");
		}
		try {
			IField[] fields = ((IType) source.getJavaElement()).getFields();
			int stats = 0;
			int inst = 0;
			int sourceField = 0;
			int publicAtr = 0;
			
			if(fields.length>0){
				Map<IField,FieldDeclaration> fieldsDeclarations = getFieldsDeclarations(fields, source);
				for (IField field : fields) {
					if ((field.getFlags() & Flags.AccStatic) != 0)
						stats++;
					else{
						inst++;
						if(fieldsDeclarations.get(field)!=null && MetricsUtils.isFieldFromSource(fieldsDeclarations.get(field)))
							sourceField++;
						if (Flags.isPublic(field.getFlags())) 
							publicAtr++;
					}

				}
			}
			
			double totalFields = inst+stats;
			double totalNonPublicFields = inst+stats-publicAtr;			
			double dam = totalFields>0?totalNonPublicFields/totalFields:0.0; 
			
			source.setValue(new Metric(NUM_FIELDS, inst));
			source.setValue(new Metric(NUM_STAT_FIELDS, stats));
			source.setValue(new Metric(MOA,sourceField));
			source.setValue(new Metric(DAM,dam));
		} catch (JavaModelException e) {
			source.setValue(new Metric(NUM_FIELDS, 0));
			source.setValue(new Metric(NUM_STAT_FIELDS, 0));
			source.setValue(new Metric(MOA,0));
			source.setValue(new Metric(DAM,0.0));
		}
	}

	private Map<IField,FieldDeclaration>  getFieldsDeclarations(IField[] fields, AbstractMetricSource source){
		Map<IField,FieldDeclaration> map = new HashMap<IField,FieldDeclaration>();
		List<FieldDeclaration> campos = new ArrayList<FieldDeclaration>();
		if(source.getASTNode() instanceof TypeDeclaration){
			TypeDeclaration td = (TypeDeclaration)source.getASTNode();
			campos.addAll(Arrays.asList(td.getFields()));
		}
		
		if(source.getASTNode() instanceof AnonymousClassDeclaration){
			AnonymousClassDeclaration acd = (AnonymousClassDeclaration)source.getASTNode();
			for(Object bd:acd.bodyDeclarations()){
				if(bd instanceof FieldDeclaration)
					campos.add((FieldDeclaration)bd);
			}			
		}
		
		if(source.getASTNode() instanceof EnumDeclaration){
			EnumDeclaration acd = (EnumDeclaration)source.getASTNode();
			for(Object bd:acd.bodyDeclarations()){
				if(bd instanceof FieldDeclaration)
					campos.add((FieldDeclaration)bd);
			}			
		}
		
		for (FieldDeclaration fd : campos) {
			for (IField field : fields) {
				for(Object fragment : fd.fragments()){
					if(fragment instanceof VariableDeclarationFragment){
						if(((VariableDeclarationFragment) fragment).getName().toString().equals(field.getElementName()))
							map.put(field, fd);
					}
				}					
			}
		}
		return map;
	}

}
