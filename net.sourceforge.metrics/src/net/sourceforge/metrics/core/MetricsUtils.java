package net.sourceforge.metrics.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

public final class MetricsUtils {
	
	static public boolean isFieldFromSource(FieldDeclaration fd){
		Type t = fd.getType();
		if(t.isSimpleType())
			return t.resolveBinding().isFromSource();
		
		//A[] returns if A is from Source
		if(t.isArrayType()){
			ArrayType at = (ArrayType)t;
			if(at.getElementType().isSimpleType() || at.getElementType().isQualifiedType())
				return at.getElementType().resolveBinding().isFromSource();			
		}
		
		//T<A>, T<A,B> - true if A is from Source or if A or B are source types.
		//Collections with source as parameters are considered because represents a relationship with a source type
		if(t.isParameterizedType()){
			ITypeBinding tb = t.resolveBinding();
			ITypeBinding[] interfaces = tb.getInterfaces();
			boolean isCollection = false;			
			for (ITypeBinding iTypeBinding : interfaces) {
				isCollection = isCollection || iTypeBinding.getBinaryName().equals("java.util.Collection");
			}
			if(isCollection){
				for (ITypeBinding typeArg : tb.getTypeArguments()) {
					if(typeArg.isFromSource())
						return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the type argument is a source one, and return all the ones involved.
	 * In this particular approach, Arrays and Parameterized types are from source if they represent
	 * @param type
	 * @return
	 */
	static public Set<String> getSourceTypeNames(ITypeBinding type){
		Set<String> nomes = new HashSet<String>();

		if(type.isFromSource()){
			nomes.add(type.getQualifiedName());
		}
			
		//A[] returns if A is from Source
		if(type.isArray() && type.getElementType().isFromSource())
			nomes.add(type.getElementType().getQualifiedName());
		
		//T<A>, T<A,B> - true if A is from Source or if A or B are source types and its a Collection
		//Collections with source as parameters are considered because represents a relationship with a source type
		if(type.isParameterizedType()){
			ITypeBinding[] interfaces = type.getInterfaces();
			boolean isCollection = false;			
			for (ITypeBinding iTypeBinding : interfaces) {
				isCollection = isCollection || iTypeBinding.getBinaryName().equals("java.util.Collection");
			}
			
			if(isCollection){
				for (ITypeBinding typeArg : type.getTypeArguments()) {
					if(typeArg.isFromSource())
						nomes.add(typeArg.getQualifiedName());
				}
			}
		}
		//System.out.println(type.getQualifiedName()+"->"+nomes);
		return nomes;
	}
}
