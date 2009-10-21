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

import org.eclipse.core.runtime.IConfigurationElement;


/**
 * Contains the specification of a calculator as contained in a plugin manifest
 *
 * @author Frank Sauer
 */
public class ExportDescriptor {
	
	private IConfigurationElement config;
	
	/**
	 * Constructor for CalculatorDescriptor.
	 */
	public ExportDescriptor(IConfigurationElement element) {
		this.config = element;
	}

	/**
	 * Returns the className.
	 * @return String
	 */
	public String getClassName() {
		return config.getAttribute("class");
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return config.getAttribute("name");
	}
	
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getDescription() {
		return config.getAttribute("description");
	}
	
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getNamespace() {
		return config.getAttribute("namespace");
	}
	
	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return getName();
	}

	/**
	 * Uses reflection to create the calculator. Calculator class must have a
	 * default no-argument constructor. If calculator cannot be created return null.
	 * @return ICalculator
	 */
	public IExporter createExporter() {
		try {
			IExporter x = (IExporter)config.createExecutableExtension("class");
			return x;
		} catch (Throwable e) {
			Log.logError("ExportDescriptor::createExporter", e);
		}
		return null;
	}
	
	/**
	 * Create a CalculatorDescriptor from a &lt;calculator&gt; element
	 * @param element
	 * @return CalculatorDescriptor
	 */
	public static ExportDescriptor createFrom(IConfigurationElement element) {
		String name = element.getAttribute("name");
		String className = element.getAttribute("class");
		if ((name != null)&&(className != null))
			return new ExportDescriptor(element);
		else return null;
	}	
	
	public String toString() {
		return "<exporter name=\"" + getName() + "\" class=\"" + getClassName() + "\">";
	}
	
}
