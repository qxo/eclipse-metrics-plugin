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
package net.sourceforge.metrics.ui.preferences;

import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Configure settings for LCOM*, E.g. the methods/attributes not to be considered.
 * 
 * @see PreferencePage
 * @author Frank Sauer
 */
public class LCOMPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * The constructor.
	 */
	public LCOMPreferencePage() {
		super(GRID);
		setPreferenceStore(MetricsPlugin.getDefault().getPreferenceStore());
		setDescription("Settings for LCOM* (Lack of Cohesion of Methods)\nWARNING: changes invalidate cache and force recalculation!");
	}

	public void init(IWorkbench workbench) {
		getPreferenceStore().setDefault("LCOM.StaticAttributes", false);
		getPreferenceStore().setDefault("LCOM.StaticMethods", false);
	}

	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor("LCOM.StaticAttributes", "Consider static fields in calculation", getFieldEditorParent()));
		addField(new BooleanFieldEditor("LCOM.StaticMethods", "Consider static methods in calculation", getFieldEditorParent()));
	}

}
