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
 * $Id: MetricsPropertyPage.java,v 1.7 2004/05/01 17:21:25 sauerf Exp $
 */
package net.sourceforge.metrics.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sourceforge.metrics.builder.MetricsNature;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Allow metrics enablement/disablement from a project's property page
 * 
 * @author Frank Sauer
 */
public class MetricsPropertyPage extends PropertyPage implements Constants {

	private Button addPattern;

	private EnableMetricsTable table;

	private Button check;

	public MetricsPropertyPage() {
		super();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		check = new Button(composite, SWT.CHECK);
		check.setText("Enable Metrics");
		IProject p = getProject();
		try {
			check.setSelection(p.hasNature(PLUGIN_ID + ".nature"));
		} catch (Throwable e) {
			Log.logError("Error gettng project nature.", e);
		}
		/*
		 * LATER, for 1.4 release table = new EnableMetricsTable(p, composite, SWT.FULL_SELECTION | SWT.CHECK); data = new GridData(GridData.FILL_BOTH | SWT.H_SCROLL | SWT.V_SCROLL); data.grabExcessHorizontalSpace = true;
		 * data.grabExcessVerticalSpace = true; table.setLayoutData(data);
		 */
		return composite;
	}

	private IProject getProject() {
		return (IProject) this.getElement().getAdapter(IProject.class);
	}

	@Override
	protected void performDefaults() {
	}

	@Override
	public boolean performOk() {
		try {
			boolean checked = check.getSelection();
			IProject p = (IProject) this.getElement().getAdapter(IProject.class);
			boolean hasNature = p.hasNature(PLUGIN_ID + ".nature");
			if (checked && !hasNature) {
				MetricsNature.addNatureToProject(p, null);
			} else if (!checked && hasNature) {
				MetricsNature.removeNatureFromProject(p, null);
			}
			/* TODO for release 1.4: table.persistState(); */
		} catch (Throwable e) {
			Log.logError("Error changing project nature.", e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#contributeButtons(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	protected void contributeButtons(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		addPattern = new Button(parent, SWT.PUSH);
		addPattern.setText("Add Exclusion Filter(s)...");
		addPattern.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				MetricDescriptor md = (MetricDescriptor) table.getSelection()[0].getData("md");
				String[] patterns = (String[]) table.getSelection()[0].getData("patterns");
				ExclusionPatternDialog d = new ExclusionPatternDialog(getShell(), md, patterns);
				if (d.open() == IDialogConstants.OK_ID) {
					table.getSelection()[0].setData("patterns", d.getPatterns());
					table.getSelection()[0].setText(1, concat(d.getPatterns()));
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		addPattern.setEnabled(false);
	}

	class EnableMetricsTable extends TableTree implements SelectionListener {

		private TableColumn exPatterns;
		private TableColumn description;
		private IProject project = null;
		private Map<String, TableTreeItem> rowLookup = new HashMap<String, TableTreeItem>();

		public EnableMetricsTable(IProject p, Composite parent, int style) {
			super(parent, style);
			getTable().setLinesVisible(true);
			getTable().setHeaderVisible(true);
			description = new TableColumn(getTable(), SWT.LEFT);
			description.setText("Metric");
			description.setWidth(250);
			exPatterns = new TableColumn(getTable(), SWT.LEFT);
			exPatterns.setText("Exclusion Filters");
			exPatterns.setWidth(250);
			addSelectionListener(this);
			this.project = p;
			initMetrics();
		}

		/**
		 * Write all state to persistent properties in the given project
		 * 
		 * @param p
		 */
		public void persistState() {
			TableTreeItem[] items = getItems();
			for (TableTreeItem item : items) {
				String id = (String) item.getData("id");
				String val = (item.getChecked()) ? "true" : "false";
				String mPatterns = item.getText(1);
				try {
					project.setPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, id + ".enabled"), val);
					project.setPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, id + ".patterns"), mPatterns);
					TableTreeItem[] folders = item.getItems();
					for (TableTreeItem folder : folders) {
						IPackageFragmentRoot f = (IPackageFragmentRoot) folder.getData("element");
						String handle = f.getHandleIdentifier();
						String fVal = (folder.getChecked()) ? "true" : "false";
						String fPatterns = folder.getText(1);
						project.setPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, id + "_" + handle + ".enabled"), fVal);
						project.setPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, id + "_" + handle + ".patterns"), fPatterns);
					}
				} catch (CoreException e) {
					Log.logError("Could not persist property", e);
				}
			}
		}

		private TableTreeItem createNewRow(MetricDescriptor md, IPackageFragmentRoot[] roots) {
			TableTreeItem item = new TableTreeItem(this, SWT.NONE);
			item.setText(0, md.getName() + " (" + md.getId() + ")");
			item.setData("id", md.getId());
			item.setData("md", md);
			item.setChecked(isEnabled(md.getId()));
			String[] patterns = getExclusionPatterns(md.getId());
			item.setData("patterns", patterns);
			item.setText(1, concat(patterns));
			rowLookup.put(md.getId(), item);
			for (IPackageFragmentRoot root : roots) {
				TableTreeItem next = new TableTreeItem(item, SWT.NONE);
				next.setText(0, root.getElementName());
				next.setData("id", md.getId());
				next.setData("element", root);
				next.setData("md", md);
				patterns = getExclusionPatterns(md.getId(), root);
				next.setData("patterns", patterns);
				next.setText(1, concat(patterns));
				next.setChecked(isEnabled(md.getId(), root));
				next.setGrayed(!item.getChecked());
			}
			return item;
		}

		/**
		 * Get the stored exclusion patterns for the given metric
		 * 
		 * @param id
		 *            metric-id
		 */
		private String[] getExclusionPatterns(String id) {
			return getExclusionPatterns(new QualifiedName(Constants.PLUGIN_ID, id + ".patterns"));
		}

		/**
		 * Get the stored exclusion patterns for the given metric and source folder
		 * 
		 * @param id
		 *            metric-id
		 */
		private String[] getExclusionPatterns(String id, IPackageFragmentRoot folder) {
			String handle = folder.getHandleIdentifier();
			return getExclusionPatterns(new QualifiedName(Constants.PLUGIN_ID, id + "_" + handle + ".patterns"));
		}

		private String[] getExclusionPatterns(QualifiedName qn) {
			try {
				String val = project.getPersistentProperty(qn);
				List<String> result = new ArrayList<String>();
				if (val != null) {
					StringTokenizer t = new StringTokenizer(val, ";");
					while (t.hasMoreTokens()) {
						result.add(t.nextToken());
					}
				}
				return result.toArray(new String[] {});
			} catch (CoreException e) {
				return new String[] {};
			}
		}

		/**
		 * Checks persisted project property
		 * 
		 * @param id
		 *            metric to be checked
		 * @param folder
		 *            source folder to be checked
		 * @return true if project properties indicate it is enabled or if such property does not exist
		 */
		private boolean isEnabled(String id, IPackageFragmentRoot folder) {
			boolean mEnabled = isEnabled(id);
			if (!mEnabled) {
				return false;
			}
			String handle = folder.getHandleIdentifier();
			try {
				String val = project.getPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, id + "_" + handle + ".enabled"));
				if (val == null) {
					return true;
				}
				return val.equals("true");
			} catch (CoreException e) {
				return true;
			}

		}

		/**
		 * Checks persisted project property
		 * 
		 * @param id
		 * @return true if project properties indicate it is enabled or if such property does not exist
		 */
		private boolean isEnabled(String id) {
			try {
				String val = project.getPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, id + ".enabled"));
				if (val == null) {
					return true;
				}
				return val.equals("true");
			} catch (CoreException e) {
				return true;
			}
		}

		/**
		 * initialize the rows (metrics and source folder children for each)
		 */
		private void initMetrics() {
			MetricsPlugin plugin = MetricsPlugin.getDefault();
			String[] names = plugin.getMetricIds();
			IPackageFragmentRoot[] roots = getPackageFragmentRoots(getProject());
			for (String name : names) {
				MetricDescriptor md = plugin.getMetricDescriptor(name);
				if (md.isAllowDisable()) {
					createNewRow(md, roots);
				}
			}
			// gray those that cannot be enabled
			TableTreeItem[] items = getItems();
			for (int i = 0; i < items.length; i++) {
				if (!allowEnable(items[i])) {
					items[i].setGrayed(true);
				}
			}
		}

		/**
		 * Get the source folder for the project
		 * 
		 * @param project
		 */
		private IPackageFragmentRoot[] getPackageFragmentRoots(IProject project) {
			IJavaProject p = JavaCore.create(project);
			List<IPackageFragmentRoot> result = new ArrayList<IPackageFragmentRoot>();
			try {
				IPackageFragmentRoot[] candidates = p.getAllPackageFragmentRoots();
				for (IPackageFragmentRoot candidate : candidates) {
					if (candidate.getKind() == IPackageFragmentRoot.K_SOURCE) {
						result.add(candidate);
					}
				}
			} catch (JavaModelException e) {
			}
			return result.toArray(new IPackageFragmentRoot[] {});
		}

		/*
		 * (non-Javadoc) react to check/uncheck events and check/uncheck and gray/ungray children and dependent metrics
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse .swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			TableTreeItem item = (TableTreeItem) e.item;
			if (item != null) {
				boolean checked = item.getChecked();
				if (e.detail == 32) {
					// check to see if metric can be enabled and undo check if
					// it can't
					TableTreeItem parent = item.getParentItem();
					if (parent == null) {
						if (checked) {
							if (allowEnable(item)) {
								enableItem(item, true, false);
							} else { // undo damage done by UI
								item.setChecked(false);
								item.setGrayed(true);
							}
						} else {
							enableItem(item, false, false);
						}
					} else {
						// disable and gray a folder if metric is disabled
						if (!parent.getChecked()) {
							item.setChecked(false);
							item.setGrayed(true);
						}
					}
				}
				addPattern.setEnabled(item.getChecked() && !item.getGrayed());
			} else {
				addPattern.setEnabled(false);
			}
		}

		/**
		 * enable/disable an item, its children and dependents elsewhere
		 * 
		 * @param item
		 * @param enable
		 * @param gray
		 */
		private void enableItem(TableTreeItem item, boolean enable, boolean gray) {
			item.setChecked(enable);
			item.setGrayed(gray);
			TableTreeItem[] children = item.getItems();
			for (TableTreeItem element2 : children) {
				element2.setChecked(enable);
				element2.setGrayed(!enable);
			}
			enableDependentMetrics((MetricDescriptor) item.getData("md"), enable);
		}

		/**
		 * enable/disable all metrics that depend on the given metric
		 * 
		 * @param descriptor
		 * @param enable
		 */
		private void enableDependentMetrics(MetricDescriptor descriptor, boolean enable) {
			MetricsPlugin plugin = MetricsPlugin.getDefault();
			String[] dependents = plugin.getDependentMetrics(descriptor);
			TableTreeItem[] items = getItems();
			if (dependents != null && dependents.length > 0) {
				for (String dependent : dependents) {
					for (TableTreeItem item : items) {
						String id = (String) item.getData("id");
						if (id.equals(dependent)) {
							if (!enable) { // simply disable
								enableItem(item, false, true);
							} else {
								// check all requirements and if ok, enable
								if (allowEnable(item)) {
									enableItem(item, true, false);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Check to see that all required metrics for item are met.
		 * 
		 * @param item
		 *            item to be checked
		 * @param j
		 *            index of metric to check
		 * @return true if all required metrics of given metric are checked
		 */
		private boolean allowEnable(TableTreeItem item) {
			MetricDescriptor md = (MetricDescriptor) item.getData("md");
			String[] requires = md.getRequiredMetricIds();
			if (requires != null && requires.length > 0) {
				for (String require : requires) {
					TableTreeItem reqItem = rowLookup.get(require);
					if (!reqItem.getChecked()) {
						return false;
					}
				}
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org .eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	private String concat(String[] patterns) {
		StringBuffer b = new StringBuffer();
		for (String pattern : patterns) {
			b.append(pattern);
			b.append(";");
		}
		String result = b.toString();
		if (result.length() > 0) {
			return result.substring(0, b.length() - 1);
		} /* else { */
		return "";
		/* } */
	}

}