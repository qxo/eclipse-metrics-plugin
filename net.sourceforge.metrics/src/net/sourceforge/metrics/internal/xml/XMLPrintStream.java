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

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Frank Sauer
 */
public class XMLPrintStream extends PrintStream {

	public final static String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param out
	 */
	public XMLPrintStream(OutputStream out) {
		super(out);
	}

	public void printXMLHeader() {
		println(XML);
	}
	
	public void indent(int level) {
		if (level > 0) {
			StringBuffer b = new StringBuffer("");
			for (int i = 0; i < level;i++) b.append("   ");
			print(b.toString());
		}
	}
	
	public String formatXSDDate(Date d) {
		synchronized(df) {
			return df.format(d).toString();
		}
	}
}
