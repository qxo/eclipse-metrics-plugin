/*
 * Copyright (c) 2003, Franz-Josef Elmer, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package classycle.graph;

/**
 * Abstract class for all algorithms based on deep search first. This class is designed in accordance with the Template Method pattern. The basic algorithm (implemented in the method {@link #process}) reads:
 * 
 * <pre>
 * vertex.visit();
 * processBefore(vertex);
 * for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i &lt; n; i++) {
 * 	processArc(vertex, vertex.getHeadVertex(i));
 * }
 * processAfter(vertex);
 * </pre>
 * 
 * The methods {@link #initializeProcessing}, {@link #processBefore}, {@link #processArc}, and {@link #processAfter} have to be implemented by concrete classes.
 * <p>
 * The class will be used by creating an instance and invoking {@link #deepSearchFirst} one or several times. Either the graph will be modified or some result objects are created which can be obtained by special methods defined in concrete
 * subclasses. Note, that a <tt>GraphProcessor</tt> is not thread-safe.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class GraphProcessor {
	/**
	 * Performs a deep search first of the specified graph. First, processing will be initialized and all vertices of the graph will be reset. Then for all unvisited vertices the private method <tt>process(Vertex)</tt> will be invoked. At
	 * last, processing will be finished.
	 * 
	 * @param graph
	 *            A directed graph.
	 */
	public void deepSearchFirst(Vertex[] graph) {
		initializeProcessing(graph);
		for (Vertex element : graph) {
			element.reset();
		}

		for (int i = 0; i < graph.length; i++) {
			if (!graph[i].isVisited()) {
				process(graph[i]);
			}
		}
		finishProcessing(graph);
	}

	/** Processes the specified vertex. */
	protected void process(Vertex vertex) {
		vertex.visit();
		processBefore(vertex);
		for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++) {
			processArc(vertex, vertex.getHeadVertex(i));
		}
		processAfter(vertex);
	}

	/**
	 * Initializes processing. Will be called in method {@link deepSearchFirst}.
	 */
	protected abstract void initializeProcessing(Vertex[] graph);

	/**
	 * Processes the specified vertex before its arcs are processed.
	 * 
	 * @param vertex
	 *            Vertex to be processed.
	 */
	protected abstract void processBefore(Vertex vertex);

	/**
	 * Processes the arc specified by tail and head vertices.
	 * 
	 * @param tail
	 *            Tail vertex of the arc.
	 * @param head
	 *            Head vertex of the arc.
	 */
	protected abstract void processArc(Vertex tail, Vertex head);

	/**
	 * Processes the specified vertex after its arcs have been processed.
	 * 
	 * @param vertex
	 *            Vertex to be processed.
	 */
	protected abstract void processAfter(Vertex vertex);

	/**
	 * Finishes processing. Will be called in method {@link deepSearchFirst}.
	 */
	protected abstract void finishProcessing(Vertex[] graph);
} // interface