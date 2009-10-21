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
import java.util.HashMap;
import java.util.Map;

import classycle.graph.StrongComponent;

import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;

/**
 * @author Frank Sauer
 */
public class Knot extends Node {

	private boolean showDetail;

	private DependencyGraphPanel panel;

	StrongComponent cycle = null;
	
	/**
	 * 
	 */
	public Knot() {
		super();
	}

	/**
	 * @param id
	 */
	public Knot(String id) {
		super(id);
	}

	/**
	 * @param id
	 * @param label
	 */
	public Knot(String id, String label, boolean showDetailMenu) {
		super(id, label);
		this.showDetail = showDetailMenu;
	}

	/**
	 * @param id
	 * @param type
	 * @param color
	 * @param label
	 */
	public Knot(String id, int type, Color color, String label) {
		super(id, type, color, label);
	}

	/**
	 * @return
	 */
	public StrongComponent getCycle() {
		return cycle;
	}

	/**
	 * @param component
	 */
	public void setCycle(StrongComponent component) {
		cycle = component;
	}

	/* Add some items to node popup menu
	 * @see com.touchgraph.graphlayout.Node#aboutToShow(java.awt.PopupMenu)
	 */
	public void aboutToShow(final PopupMenu nodePopup) {
		super.aboutToShow(nodePopup);
		final MenuItem path = new MenuItem("Find Shortest Path");
		nodePopup.add(path);
		path.addActionListener(new ActionListener() {
	
			public void actionPerformed(ActionEvent e) {
				findShortestPath();
			}
		});
		
		if (showDetail) {
			final MenuItem analyze = new MenuItem("Analyze Details");
			nodePopup.add(analyze);
			analyze.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					analyze();
				}
			});
		}
	}

	private void analyze() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				Map dependencies = new HashMap();
				Map packages = new HashMap();
				new TangleAnalyzer(cycle, dependencies, packages).analyze();
				if (dependencies.size()>0) { // 0 if cancelled by user
					try {
						panel.createDependencies(dependencies, packages);
					} catch (TGException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	private void findShortestPath() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				PathFinder.showPathFinderUI(cycle);
			}
		});
		t.start();
	}
	
	/**
	 * @param panel
	 */
	public void setDependencyPanel(DependencyGraphPanel panel) {
		this.panel = panel;
	}
}
