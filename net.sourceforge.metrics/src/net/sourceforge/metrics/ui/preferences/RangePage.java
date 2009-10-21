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
 * created on March 25, 2003 
 */
package net.sourceforge.metrics.ui.preferences;

import net.sourceforge.metrics.core.MetricDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * preference page allowing the user to set safe ranges for the metrics
 * and the corresponding tip to solve out-of-range problems.
 * 
 * @author Frank Sauer
 */
public class RangePage extends PreferencePage implements IWorkbenchPreferencePage {


	private static class MetricDescriptorLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element == null) return "";
			MetricDescriptor md = (MetricDescriptor)element;
			switch(columnIndex) {
				case 0: {
					return md.getName();
				}
				case 1: {
					return md.getLevel();
				}
				case 2: {
					Double min = md.getMin();
					return (min==null)?"":min.toString();
				}
				case 3: {
					Double max = md.getMax();
					return (max==null)?"":max.toString();
				}
				case 4: {
					String hint = md.getHint();
					return (hint==null)?"":md.getHint();
				}
				default: return "";
			}
		}

	}
	
	private static class RangeCellModifier implements ICellModifier {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			return !property.equals("NAME") && !property.equals("LEVEL");
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			MetricDescriptor md = (MetricDescriptor)element;
			if (property.equals("MIN")) {
				Double min = md.getMin();
				return (min==null)?"":min.toString();				
			} else if (property.equals("MAX")) {
				Double max = md.getMax();
				return (max==null)?"":max.toString();				
			} else if (property.equals("HINT")) {
				String hint = md.getHint();
				return (hint==null)?"":md.getHint();
			}
			return "";
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			TableItem item = (TableItem)element;
			MetricDescriptor md = (MetricDescriptor)item.getData();
			String values[] = new String[]{
				item.getText(0),
				item.getText(1),
				item.getText(2),
				item.getText(3),
				item.getText(4)
			};
			if (property.equals("MIN")) {
				String minStr = (String)value;
				try {
					Double min =new Double(minStr);
					md.setMin(min);
					values[2] = minStr;			
				} catch (NumberFormatException e) {
					values[2] = "";			
				}
			} else if (property.equals("MAX")) {
				String maxStr = (String)value;
				try {
					Double max =new Double(maxStr);
					md.setMax(max);
					values[3] = maxStr;			
				} catch (NumberFormatException e) {
					values[3] = "";
				}
			} else if (property.equals("HINT")) {
				values[4] = value.toString();
				md.setHint(values[4]);
			}
			item.setText(values);
		}
	}
	
	private String[] properties = new String[]{"NAME", "LEVEL", "MIN", "MAX", "HINT"};
	private TableViewer tv;
	private ITableLabelProvider lp = new MetricDescriptorLabelProvider();
	private ICellModifier modifier = new RangeCellModifier();
	
	/**
	 * 
	 */
	public RangePage() {
		super();
		setTitle("Safe Metrics Ranges");		
		setDescription("Here you can set the safe range for each metric.\nMetric values outside these ranges result in warnings if warnings are enabled.");
	}

	protected Control createContents(Composite ancestor) {
		
		Composite parent= new Composite(ancestor, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);	
		GridData data;
				
		Table table= new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		data= new GridData(GridData.FILL_BOTH);
		data.widthHint = 400;
		table.setLayoutData(data);
				
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		
		
		TableColumn column1= new TableColumn(table, SWT.NULL);
		column1.setText("Metric");
		TableColumn column2= new TableColumn(table, SWT.NULL);
		column2.setText("Level");
		TableColumn column3= new TableColumn(table, SWT.NULL);
		column3.setText("Min");
		column3.setAlignment(SWT.RIGHT);
		TableColumn column4= new TableColumn(table, SWT.NULL);
		column4.setText("Max");
		column4.setAlignment(SWT.RIGHT);
		TableColumn column5= new TableColumn(table, SWT.NULL);
		column5.setText("Hint for fix");
		tv = new TableViewer(table);
		tv.setLabelProvider(lp);
		tv.setCellModifier(modifier);
		tv.setColumnProperties(properties);
		tv.setCellEditors(new CellEditor[] {
			null,
			null,
			new TextCellEditor(table /*BUG822672, SWT.NONE*/),
			new TextCellEditor(table /*BUG822672, SWT.NONE*/),
			new TextCellEditor(table /*BUG822672, SWT.NONE*/)
		});
		populateTable();
		column1.pack();
		column2.pack();
		column3.pack();
		column4.pack();
		column5.pack();
		
		return parent;	
	}

	/**
	 * 
	 */
	private void populateTable() {
		String[] ids = MetricsPlugin.getDefault().getMetricIds();
		for (int i = 0; i < ids.length; i++) {
			MetricDescriptor md = MetricsPlugin.getDefault().getMetricDescriptor(ids[i]);
			tv.add(md);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		if (MessageDialog.openConfirm(getShell(), "Please Confirm", "Resets all values to those specified in manifests.\nCancel will not undo this.\nAre you Sure?")) {
			TableItem[] items = tv.getTable().getItems();
			if (items != null) {
				for (int i = 0; i < items.length;i++) {
					MetricDescriptor md = (MetricDescriptor)items[i].getData();
					if (md != null) {
						md.resetToDefaults();
					}
				}
			}
			tv.getTable().removeAll();
			populateTable();
		}
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		TableItem[] items = tv.getTable().getItems();
		if (items != null) {
			for (int i = 0; i < items.length;i++) {
				MetricDescriptor md = (MetricDescriptor)items[i].getData();
				if (md != null) {
					md.copyToPreferences();
				}
			}
		}
		return super.performOk();
	}
    
}
