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
 * created on Jan 13, 2003
 */
package net.sourceforge.metrics.core;

import net.sourceforge.metrics.calculators.Calculator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Contains the specification of a calculator as contained in a plugin manifest
 * 
 * @author Frank Sauer
 */
public class CalculatorDescriptor {

	private IConfigurationElement config;

	/**
	 * Constructor for CalculatorDescriptor.
	 */
	public CalculatorDescriptor(IConfigurationElement element) {
		this.config = element;
	}

	/**
	 * Returns the className.
	 * 
	 * @return String
	 */
	public String getClassName() {
		return config.getAttribute("calculatorClass");
	}

	/**
	 * Returns the level.
	 * 
	 * @return String
	 */
	public String getLevel() {
		return config.getAttribute("level");
	}

	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return config.getAttribute("name");
	}

	/**
	 * Returns the id.
	 * 
	 * @return String
	 */
	public String getId() {
		return getName();
	}

	/**
	 * Uses reflection to create the calculator. Calculator class must have a default no-argument constructor. If calculator cannot be created return null.
	 * 
	 * @return ICalculator
	 */
	public ICalculator createCalculator() {
		try {
			Calculator c = (Calculator) config.createExecutableExtension("calculatorClass");
			c.setDescriptor(this);
			return c;
		} catch (CoreException e) {
			Log.logError("CalculatorDescriptor::createCalculator", e);
		}
		return null;
	}

	/**
	 * Create a CalculatorDescriptor from a &lt;calculator&gt; element
	 * 
	 * @param element
	 * @return CalculatorDescriptor
	 */
	public static CalculatorDescriptor createFrom(IConfigurationElement element) {
		String name = element.getAttribute("name");
		String className = element.getAttribute("calculatorClass");
		String level = element.getAttribute("level");
		if ((name != null) && (className != null) && (level != null)) {
			return new CalculatorDescriptor(element);
		} /* else { */
		return null;
		/* } */
	}

	@Override
	public String toString() {
		return "<calculator name=\"" + getName() + "\" class=\"" + getClassName() + "\">";
	}

}
