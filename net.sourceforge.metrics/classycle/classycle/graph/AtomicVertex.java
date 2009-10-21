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
 * A subclass of {@link Vertex} with the following additional properties:
 * <ul>
 * <li>A flag indicating whether this vertex belongs to the graph or not.
 * <li>The order of visiting.
 * <li>The low function.
 * </ul>
 * The last two properties are used in Tarjan's algorithm to find the strong components (see {@link StrongComponentProcessor}).
 * 
 * @author Franz-Josef Elmer
 */
public class AtomicVertex extends Vertex {
	private boolean _graphVertex;
	private int _order;
	private int _low;

	/** Creates an instance for the specified attributes. */
	public AtomicVertex(Attributes attributes) {
		super(attributes);
	}

	/**
	 * Reset this instance. That is, it becomes unvisited vertex of a graph with order and low set to -1;
	 */
	@Override
	public void reset() {
		super.reset();
		_graphVertex = true;
		_order = -1;
		_low = -1;
	}

	/** Returns <tt>true</tt> if this vertex belongs to a graph. */
	public boolean isGraphVertex() {
		return _graphVertex;
	}

	/** Returns the order of visiting. */
	public int getOrder() {
		return _order;
	}

	/** Sets the order of visiting. */
	public void setOrder(int order) {
		_order = order;
	}

	/** Returns the current value of the low function. */
	public int getLow() {
		return _low;
	}

	/** Sets the current value of the low function. */
	public void setLow(int low) {
		_low = low;
	}
} // class