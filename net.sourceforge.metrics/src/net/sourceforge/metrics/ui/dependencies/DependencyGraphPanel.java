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
 * $Id: DependencyGraphPanel.java,v 1.21 2004/06/05 14:48:27 sauerf Exp $
 */
package net.sourceforge.metrics.ui.dependencies;

import java.awt.Color;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import classycle.graph.AtomicVertex;
import classycle.graph.StrongComponent;
import classycle.graph.StrongComponentProcessor;
import classycle.graph.Vertex;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.GLPanel;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGPanel;

/**
 * @author Frank Sauer
 */
public class DependencyGraphPanel extends GLPanel {

	private static final long serialVersionUID = 4131385063548963525L;
	private MenuItem topo;
	private Vertex[] vgraph;
	private Menu tangleMenu;
	private boolean showDetailMenu = true;

	public DependencyGraphPanel() {
		super();
	}

	/**
	 * 
	 */
	public DependencyGraphPanel(Map dependencies) {
		this();
		try {
			createDependencies(dependencies);
		} catch (TGException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.touchgraph.graphlayout.GLPanel#buildPanel()
	 */
	@Override
	public void buildPanel() {
		super.buildPanel();
		PopupMenu popup = this.getGLPopup();
		tangleMenu = new Menu("Show Tangle ");
		popup.addSeparator();
		popup.add(tangleMenu);
		MenuItem paths = new MenuItem("Find Shortest Path");
		paths.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				findShortestPaths();
			}
		});
		popup.add(paths);
		topo = new MenuItem("Topological Sort");
		topo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				topoSort();
			}
		});
		popup.add(topo);
	}

	public void findShortestPaths() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				PathFinder.showPathFinderUI(vgraph);
			}
		});
		t.start();
	}

	public void topoSort() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				TopoSortDialog.showUI(vgraph);
			}
		});
		t.start();
	}

	private void initTGPanel() {
		tgPanel.clearSelect();
		tgPanel.clearAll();
		tgPanel.setBackColor(getGraphBackground());
		tgPanel.setBackground(TGPanel.BACK_COLOR);
		tgPanel.repaint();
	}

	public void showMessage(String message) {
		try {
			initTGPanel();
			tangleMenu.removeAll();
			topo.setEnabled(false);
			Node mNode = addNode("_messageNode");
			mNode.setLabel(message);
			tgPanel.setLocale(mNode, 1);
			tgPanel.setSelect(mNode);
			getHVScroll().slowScrollToCenter(mNode);
		} catch (TGException e) {
			Log.logError("DepencyGraphPanel.showMessage:", e);
		}
	}

	/**
	 * @param dependencies
	 * @param packages
	 */
	public void createDependencies(Map dependencies, Map packages) throws TGException {
		initTGPanel();
		tangleMenu.removeAll();
		topo.setEnabled(true);
		int max = -1;
		Node center = null;
		showDetailMenu = (packages == null);
		StrongComponent[] components = calculateCycles(dependencies);
		for (Iterator i = dependencies.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			Set deps = (Set) dependencies.get(name);
			Node from = addNode(name);
			if (deps.size() > max) {
				max = deps.size();
				center = from;
			}
			for (Iterator d = deps.iterator(); d.hasNext();) {
				Node to = addNode((String) d.next());
				addEdge(from, to, components);
			}

		}
		if (packages != null) {
			for (Iterator i = packages.keySet().iterator(); i.hasNext();) {
				String name = (String) i.next();
				Set deps = (Set) packages.get(name);
				Node from = addNode(name);
				from.setBackColor(Color.green);
				from.setTextColor(Color.black);
				from.setNodeType(Node.TYPE_ROUNDRECT);
				for (Iterator d = deps.iterator(); d.hasNext();) {
					Node to = addNode((String) d.next());
					addEdge(from, to);
				}

			}
		}
		if (center != null) { // BUG #827055
			tgPanel.setLocale(center, 1);
			tgPanel.setSelect(center);
			getHVScroll().slowScrollToCenter(center);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.touchgraph.graphlayout.GLPanel#randomGraph()
	 */
	public void createDependencies(Map source) throws TGException {
		createDependencies(source, null);
	}

	/**
	 * Calculate strongly connected components in dependency graph, basically adaper code to map different graph representations
	 * 
	 * @param efferent
	 * @return
	 */
	private StrongComponent[] calculateCycles(Map efferent) {
		List<Vertex> graph = new ArrayList<Vertex>();
		Map<String, Vertex> done = new HashMap<String, Vertex>();
		for (Iterator i = efferent.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Vertex from = done.get(key);
			if (from == null) {
				from = new AtomicVertex(new PackageAttributes(key));
				done.put(key, from);
				graph.add(from);
			}
			Set deps = (Set) efferent.get(key);
			for (Iterator j = deps.iterator(); j.hasNext();) {
				String dep = (String) j.next();
				Vertex to = done.get(dep);
				if (to == null) {
					to = new AtomicVertex(new PackageAttributes(dep));
					done.put(dep, to);
					graph.add(to);
				}
				from.addOutgoingArcTo(to);
			}
		}
		vgraph = graph.toArray(new Vertex[] {});
		StrongComponentProcessor scp = new StrongComponentProcessor();
		scp.deepSearchFirst(vgraph);
		StrongComponent[] comps = scp.getStrongComponents();
		return comps;
	}

	/**
	 * cluster strongly connected packages (cycles) together, closer than packages not part of the strong connection.
	 * 
	 * @param to
	 * @param from
	 */
	private void addEdge(Node from, Node to, StrongComponent[] comps) {
		Edge e = tgPanel.findEdge(from, to);
		if (e == null) {
			e = new Edge(from, to);
			int strong = isStrong(from, to, comps);
			if (strong >= 0) {
				addKnot(from, to, comps[strong], strong);
				e.setLength(Edge.DEFAULT_LENGTH);
				e.setColor(Color.red);
				to.setBackColor(Color.red);
				from.setBackColor(Color.red);
			} else {
				e.setLength(Edge.DEFAULT_LENGTH + 20);
			}
			tgPanel.addEdge(e);
		}
	}

	private void addEdge(Node from, Node to) {
		Edge e = tgPanel.findEdge(from, to);
		if (e == null) {
			e = new Edge(from, to);
			e.setColor(Color.green);
			e.setLength(Edge.DEFAULT_LENGTH - 30);
			tgPanel.addEdge(e);
		}
	}

	/**
	 * A knot sits at the center of a strongly connected component
	 * 
	 * @param from
	 * @param to
	 * @param component
	 * @param knots
	 */
	private void addKnot(Node from, Node to, StrongComponent component, int index) {
		try {
			Knot knot = (Knot) tgPanel.findNode("knot" + index);
			if (knot == null) {
				int length = component.getNumberOfVertices();
				String lbl = "" + length + "/" + component.getDiameter();
				knot = new Knot("knot" + index, lbl, showDetailMenu);
				knot.setCycle(component);
				knot.setDependencyPanel(this);
				knot.setBackColor(Color.yellow);
				knot.setNodeTextColor(Color.black);
				knot.setTextColor(Color.black);
				knot.setNodeType(Node.TYPE_CIRCLE);
				tgPanel.addNode(knot);
				if (length > 1) {
					addKnotToMenu(from.getLabel()[0] + " et al. (" + lbl + ")", knot);
				}
			}
			addKnotEdge(knot, from);
			addKnotEdge(knot, to);
		} catch (TGException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param string
	 * @param knot
	 */
	private void addKnotToMenu(String label, final Node knot) {
		MenuItem knotItem = new MenuItem(label);
		tangleMenu.add(knotItem);
		topo.setEnabled(false); // no topological sort when cycles present
		knotItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					tgPanel.setLocale(knot, 1);
					tgPanel.setSelect(knot);
					getHVScroll().slowScrollToCenter(knot);
				} catch (TGException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void addKnotEdge(Node from, Node knot) {
		Edge e = new Edge(from, knot);
		e.setColor(Color.yellow);
		tgPanel.addEdge(e);
	}

	/**
	 * find out if two nodes are part of the same component
	 * 
	 * @param from
	 * @param to
	 * @param comps
	 * @return index of component if one exists, otherwise -1
	 */
	private int isStrong(Node from, Node to, StrongComponent[] comps) {
		for (int i = 0; i < comps.length; i++) {
			int count = 0;
			for (int j = 0; j < comps[i].getNumberOfVertices(); j++) {
				Vertex v = comps[i].getVertex(j);
				if (v.getAttributes().equals(from.getID())) {
					count++;
				}
				if (v.getAttributes().equals(to.getID())) {
					count++;
				}
			}
			if (count == 2) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Adds a node for the given id, unless it already exists. The node's label will be the id, unless it starts with an "=" in which case it is treated as a IJavaElement handle and the elements name will be used as the label.
	 * 
	 * @param id
	 * @return
	 * @throws TGException
	 */
	private Node addNode(String id) throws TGException {
		Node n = tgPanel.findNode(id);
		String name = id;
		if (name.length() == 0) {
			name = "(Default Package)";
		}
		if (id.startsWith("=")) {
			IJavaElement element = JavaCore.create(id);
			name = element.getElementName();
		}
		if (n == null) {
			n = new EclipseNode(id, name);
			tgPanel.addNode(n);
		}
		return n;
	}

	private Color getGraphBackground() {
		RGB color = PreferenceConverter.getColor(MetricsPlugin.getDefault().getPreferenceStore(), "METRICS.depGR_background");
		return new Color(color.red, color.green, color.blue);
	}
}
