package net.sourceforge.metrics.ui.dependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import classycle.graph.StrongComponent;
import classycle.graph.Vertex;

/**
 * Finds shortest paths in a strong component
 * @author Frank Sauer
 */
public class PathFinder {

	private Vertex[] graph;
	
	public PathFinder(StrongComponent cycle) {
		this(getVerticesFromCycle(cycle));
	}
	
	public PathFinder(Vertex[] graph) {
		this.graph = graph;
	}
	
	public Vertex[] getVertices() {
		return graph;
	}
	
	public static Vertex[] getVerticesFromCycle(StrongComponent cycle) {
		Vertex[] result = new Vertex[cycle.getNumberOfVertices()];
		for (int i = 0; i < result.length;i++) {
			result[i] = cycle.getVertex(i);
		}
		return result;
	}
	
	/**
	 * Should only be called with from and to as vertices of the strong
	 * component used to create this TangleAnalyzer, otherwise no
	 * guarantee exists that a path can be found.
	 * Since all vertices in a strong component are reachable from
	 * each other, there must be a shortest path from every vertex
	 * of the tangle to every other vertex of the tangle, and this
	 * method should therefore never return null. Just in case, if no
	 * path is found, this method returns null. 
	 * This method implements the breadth-first search from Cormen et al
	 * page 470
	 * @param from	starting vertex
	 * @param to	target vertex
	 * @return		arrsy of Vertex starting with from, ending with to
	 */
	public Vertex[] findShortestPath(Vertex from, Vertex to) {
		// reset all vertices to WHITE
		int length = graph.length;
		for (int i = 0; i < length; i++) {
			graph[i].reset();
		}
		// BFS from Cormen et al, page 470
		Queue q = new Queue();
		Map p = new HashMap();
		from.visit();
		q.enqueue(from);
		while (q.size() != 0) {
			Vertex u = q.head();
			int arcs = u.getNumberOfOutgoingArcs();
			for (int a = 0; a < arcs; a++) {
				Vertex v = u.getHeadVertex(a);
				if (!v.isVisited()) {
					v.visit();
					p.put(v,u);
					q.enqueue(v);
				}
			}
			q.dequeue();			
		}
		List result = new ArrayList();
		try {
			buildPath(from, to, p, result);
			return (Vertex[])result.toArray(new Vertex[]{});
		} catch (IllegalArgumentException e) {
			return null; // no path found
		}
	}
	
	/**
	 * recursively build the path from the precedence map calculated
	 * by the BFS
	 * @param from		starting vertex
	 * @param to		target vertex
	 * @param p			precedence map from BFS
	 * @param result	path is collected in here
	 * @throws IllegalArgumentException if path cannot be constructed
	 */
	private void buildPath(Vertex from, Vertex to, Map p, List result) throws IllegalArgumentException {
		if (from == to) {
			result.add(from);
		} else {
			Vertex pv = (Vertex)p.get(to);
			if (pv == null) throw new IllegalArgumentException("No path found");
			// recurse
			buildPath(from, pv, p, result);
			result.add(to);
		}
	}	
	/**
	 * Simple Queue interface derived from LinkedList to make BFS
	 * more readable and related to pseudocode in the textbook.
	 * 
	 * @author Frank Sauer
	 */
	static class Queue extends LinkedList {
		
		public void enqueue(Vertex v) {
			super.addFirst(v);
		}
		
		public Vertex dequeue() {
			return (Vertex)super.removeLast();
		}
		
		public Vertex head() {
			return (Vertex)super.getLast();
		}
	}
	
	public static void showPathFinderUI(StrongComponent cycle) {
		new PathFinder(cycle).showUI(true);
	}
	
	public static void showPathFinderUI(Vertex[] graph) {
		new PathFinder(graph).showUI(false);
	}
	
	private void showUI(final boolean showReverse) {
		final Display d = Display.getDefault();
		d.syncExec(new Runnable() {

			public void run() {
				Shell shell = new Shell(d);
				PathFinderDialog pfd = new PathFinderDialog(shell, PathFinder.this, showReverse);
				pfd.open();
			}
		});
	}
}
