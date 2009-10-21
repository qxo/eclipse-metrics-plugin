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
package net.sourceforge.metrics.ui.dependencies;

import java.awt.Color;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sourceforge.metrics.core.Log;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

import com.touchgraph.graphlayout.Node;

/**
 * Adds the "Open In Editor" popup item to a node if its ID is a
 * handle identifier for a IJavaElement
 * 
 * @author Frank Sauer
 */
public class EclipseNode extends Node {

	/**
	 * 
	 */
	public EclipseNode() {
		super();
	}

	/**
	 * @param id
	 */
	public EclipseNode(String id) {
		super(id);
	}

	/**
	 * @param id
	 * @param label
	 */
	public EclipseNode(String id, String label) {
		super(id, label);
	}

	/**
	 * @param id
	 * @param type
	 * @param color
	 * @param label
	 */
	public EclipseNode(String id, int type, Color color, String label) {
		super(id, type, color, label);
	}

	/* (non-Javadoc)
	 * @see com.touchgraph.graphlayout.Node#aboutToShow(java.awt.PopupMenu)
	 */
	public void aboutToShow(PopupMenu nodePopup) {
		super.aboutToShow(nodePopup);
		// if id is a IJavaElement handleIdentifier add open in Editor item
		if (getID().startsWith("=")) {
			MenuItem open = new MenuItem("Open In Editor");
			nodePopup.add(open);
			open.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					openInEditor();
				}
			});
		}
	}
	private void openInEditor() {
		Display d = Display.getDefault();
		d.asyncExec(new Runnable() {

			public void run() {
				try {
					IJavaElement element = JavaCore.create(getID());
					ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
					IEditorPart javaEditor = JavaUI.openInEditor(cu);
					JavaUI.revealInEditor(javaEditor, element);
				} catch (PartInitException e) {
					Log.logError("Node.openInEditor", e);
				} catch (JavaModelException e) {
					Log.logError("Node.openInEditor", e);
				}
			}
		});
	}	

}
