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
 * $Id: ColorPreferencePage.java,v 1.2 2004/05/01 19:23:18 sauerf Exp $
 */
package net.sourceforge.metrics.ui.preferences;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Allow user to change color for inrange and out-of-range metrics
 * 
 * @author Frank Sauer
 */
public class ColorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, Constants {

	/**
	 * @param style
	 */
	public ColorPreferencePage() {
		super(GRID);
		setPreferenceStore(MetricsPlugin.getDefault().getPreferenceStore());
		setDescription("Color preferences for metrics and dependency graph view");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors ()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new ColorFieldEditor("METRICS.defaultColor", "Default within range color", getFieldEditorParent()));
		addField(new ColorFieldEditor("METRICS.linkedColor", "Within range linked color", getFieldEditorParent()));
		addField(new ColorFieldEditor("METRICS.outOfRangeColor", "Out-of-range color", getFieldEditorParent()));
		addField(new ColorFieldEditor("METRICS.depGR_background", "Dependency Graph Background Color", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
