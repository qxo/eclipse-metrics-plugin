/*
 * Created on May 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.metrics.ui.dependencies;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import classycle.graph.Attributes;

public class PackageAttributes implements Attributes {

	private String name = null;

	public PackageAttributes(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof PackageAttributes) {
			return name.equals(((PackageAttributes) o).name);
		}
		if (o instanceof String) {
			return name.equals(o);
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return
	 */
	public String getLabel() {
		if (name.startsWith("=")) {
			IJavaElement element = JavaCore.create(name);
			return element.getElementName();
		} /* else { */
		return name;
		/* } */
	}

	public IJavaElement getJavaElement() {
		if (name.startsWith("=")) {
			return JavaCore.create(name);
		} /* else { */
		return null;
		/* } */
	}
}