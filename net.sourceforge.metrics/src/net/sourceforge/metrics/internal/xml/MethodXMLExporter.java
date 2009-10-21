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
package net.sourceforge.metrics.internal.xml;

import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Export method metrics to an XML report
 * 
 * @author Frank Sauer
 */
public class MethodXMLExporter extends MetricsExporter implements IXMLExporter {

	public MethodXMLExporter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.export.xml.IExporter#export(net.sourceforge.metrics .core.sources.AbstractMetricSource, net.sourceforge.metrics.export.xml.XMLPrintStream, int)
	 */
	public void export(AbstractMetricSource source, XMLPrintStream out, int level, IProgressMonitor monitor) {
		printOpeningTag(source, out, level);
		printMetrics(source, out, level + 1);
		printClosingTag(out, level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.metrics.internal.xml.MetricsExporter#getTagName()
	 */
	@Override
	public String getTagName() {
		return "Method";
	}

}
