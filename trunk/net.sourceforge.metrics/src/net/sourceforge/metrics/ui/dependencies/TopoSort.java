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

import classycle.graph.GraphProcessor;
import classycle.graph.StrongComponent;
import classycle.graph.StrongComponentProcessor;
import classycle.graph.Vertex;

/**
 * Performs a topological sort on a graph
 * 
 * @author Frank Sauer
 */
public class TopoSort extends GraphProcessor {

	private Vertex[] sorted = null;
	int finish = 0;
	
	public TopoSort() {
		super();
	}
	
	/**
	 * return the graph topologically sorted, or null if the
	 * graph has cycles and no topological sort order is possible
	 */
	public Vertex[] sort(Vertex[] graph) {
		StrongComponentProcessor cycleTest = new StrongComponentProcessor();
		cycleTest.deepSearchFirst(graph);
		StrongComponent[] comps = cycleTest.getStrongComponents();
		if (comps.length == graph.length) {
			sorted = new Vertex[graph.length];
			super.deepSearchFirst(comps);
			return sorted;
		} else return null;
	}
	
	/* (non-Javadoc)
	 * @see classycle.graph.GraphProcessor#initializeProcessing(classycle.graph.Vertex[])
	 */
	protected void initializeProcessing(Vertex[] graph) {
	}

	/* (non-Javadoc)
	 * @see classycle.graph.GraphProcessor#processBefore(classycle.graph.Vertex)
	 */
	protected void processBefore(Vertex vertex) {
		castAsStrongComponent(vertex);
	}

	/* (non-Javadoc)
	 * @see classycle.graph.GraphProcessor#processArc(classycle.graph.Vertex, classycle.graph.Vertex)
	 */
	protected void processArc(Vertex tail, Vertex head) {
		castAsStrongComponent(tail);
		StrongComponent h = castAsStrongComponent(head);
	  	if (!h.isVisited()) {
			process(h);
	  	} 
	}

	/* (non-Javadoc)
	 * @see classycle.graph.GraphProcessor#processAfter(classycle.graph.Vertex)
	 */
	protected void processAfter(Vertex vertex) {
		StrongComponent component = castAsStrongComponent(vertex);
		sorted[finish++] = component.getVertex(0);
	}

	/* (non-Javadoc)
	 * @see classycle.graph.GraphProcessor#finishProcessing(classycle.graph.Vertex[])
	 */
	protected void finishProcessing(Vertex[] graph) {
	}
	
	/**
	 *  Casts the specified vertex as an {@link StrongComponent}.
	 *  @throws IllegalArgumentException if <tt>vertex</tt> is not an instance
	 *          of {@link StrongComponent}.
	 */
	private StrongComponent castAsStrongComponent(Vertex vertex)
	{
	  if (vertex instanceof StrongComponent)
	  {
		return (StrongComponent) vertex;
	  }
	  else
	  {
		throw new IllegalArgumentException(vertex
				+ " is not an instance of StrongComponent");
	  }
	}
}
