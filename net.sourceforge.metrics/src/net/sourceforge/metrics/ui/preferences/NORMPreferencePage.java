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

import java.util.StringTokenizer;

import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Configure settings for NORM, E.g. the methods not to be counted.
 * 
 * @see PreferencePage
 * @author Frank Sauer
 */
public class NORMPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * The constructor.
	 */
	public NORMPreferencePage() {
		super(GRID);
		setPreferenceStore(MetricsPlugin.getDefault().getPreferenceStore());
		setDescription("Settings for NORM (Number of Overridden Methods)\nWARNING: changes invalidate cache and force recalculation!");
	}

	public void init(IWorkbench workbench)  {
		getPreferenceStore().setDefault("NORM.Abstract", false);
		getPreferenceStore().setDefault("NORM.Super", false);
		getPreferenceStore().setDefault("NORM.ExludeList", "toString, hashCode, equals");
	}

	public void createFieldEditors() {
		addField(new BooleanFieldEditor("NORM.Abstract","Count abstract Inherited Methods", getFieldEditorParent()));
		addField(new BooleanFieldEditor("NORM.Super","Count methods invoking super", getFieldEditorParent()));
		addField(new ListEditor("NORM.ExludeList","Exclude the following methods:", getFieldEditorParent()) {
			protected String createList(String[] items) {
				StringBuffer b = new StringBuffer();
				for (int i = 0; i < items.length;i++) {
					b.append(items[i]).append(",");
				}
				return b.substring(0,b.length()-1);
			}
			protected String getNewInputObject() {
				InputDialog input = new InputDialog(getShell(),"New Excluded method", "Please type a method name","", null);
				input.open();
				if (input.getReturnCode() == InputDialog.OK) 
					return input.getValue();
				else return null;
			}
			protected String[] parseString(String stringList) {				
				StringTokenizer t = new StringTokenizer(stringList, ",");
				int length = t.countTokens();
				String[] items = new String[length];
				for (int i = 0; i < length; i++) {
					items[i] = t.nextToken().trim();
				}
				return items;
			}
		});
	}
	
}
