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

import java.util.ArrayList;
import java.util.List;

/**
 *  The base class for any type of vertex in a directed graph.
 *  <p>
 *  A <tt>Vertex</tt> holds an {@link Attributes} object which encapsulates
 *  all properties of the vertex which is not necessary to know for
 *  parsing a graph in a {@link GraphProcessor}. Only the visited flag will
 *  be manipulated during parsing.
 *  <p>
 *  A <tt>Vertex</tt> knows the head and tail vertices of all its outgoing
 *  and incoming arcs. When a head vertex is added by the method
 *  {@link #addOutgoingArcTo} also the corresponding incoming arc is built
 *  in the head vertex. The same is true the other way around. Note,
 *  that multi-arcs are not possible. That is, adding an already added
 *  head/tail vertex again as a head/tail vertex will be ignored.
 *
 *  @author Franz-Josef Elmer
 */
public class Vertex
{
  private final List _heads = new ArrayList();
  private final List _tails = new ArrayList();
  private final Attributes _attributes;
  private boolean _visited;

  /** Create a new instance for the specified attributes. */
  public Vertex(Attributes attributes)
  {
    _attributes = attributes;
  }

  /** Returns the attributes. */
  public Attributes getAttributes()
  {
    return _attributes;
  }

  /**
   * Returns the number of outgoing arcs. This is equivalent to the number
   * of head vertices.
   */
  public int getNumberOfOutgoingArcs()
  {
    return _heads.size();
  }

  /** Returns the head vertex of the specified outgoing arc. */
  public Vertex getHeadVertex(int index)
  {
    return (Vertex) _heads.get(index);
  }

  /**
   * Adds an outgoing arc to the specified vertex. Also calls
   * {@link #addIncomingArcTo} for <tt>headVertex</tt> with <tt>this</tt> as
   * the argument. Does nothing if <tt>headVertex</tt> is the head
   * vertex of an already existing outgoing arc.
   * @param headVertex Head vertex to be added to establish a new outgoing arc.
   *        <tt>Null</tt> is not allowed.
   */
  public void addOutgoingArcTo(Vertex headVertex)
  {
    if (!_heads.contains(headVertex))
    {
      _heads.add(headVertex);
      headVertex.addIncomingArcTo(this);
    }
  }

  /**
   * Returns the number of incoming arcs. This is equivalent to the number
   * of tail vertices.
   */
  public int getNumberOfIncomingArcs()
  {
    return _tails.size();
  }

  /** Returns the tail vertex of the specified outgoing arc. */
  public Vertex getTailVertex(int index)
  {
    return (Vertex) _tails.get(index);
  }

  /**
   * Adds an incoming arc to the specified vertex. Also calls
   * {@link #addOutgoingArcTo} for <tt>tailVertex</tt> with <tt>this</tt> as
   * the argument. Does nothing if <tt>tailVertex</tt> is the
   * tail vertex of an already existing incoming arc.
   * @param tailVertex Tail vertex to be added to establish a new incoming arc.
   *        <tt>Null</tt> is not allowed.
   */
  public void addIncomingArcTo(Vertex tailVertex)
  {
    if (!_tails.contains(tailVertex))
    {
      _tails.add(tailVertex);
      tailVertex.addOutgoingArcTo(this);
    }
  }

  /**
   *  Reset this vertex. That is, the visited flag is set to <tt>false</tt>.
   */
  public void reset()
  {
    _visited = false;
  }

  /**
   *  Marks this instance as visited.
   *  That is, the visited flag becomes <tt>true</tt>.
   */
  public void visit()
  {
    _visited = true;
  }

  /** Returns the visited flag. */
  public boolean isVisited()
  {
    return _visited;
  }

  /**
   *  Returns <tt>toString()</tt> of the attributes and the number of
   *  incoming and outgoing arcs.
   */
  public String toString()
  {
    StringBuffer result = new StringBuffer();
    result.append(getAttributes() == null ? super.toString()
                                          : getAttributes().toString());
    result.append(": ").append(getNumberOfIncomingArcs());
    result.append(" incoming arc(s), ").append(getNumberOfOutgoingArcs());
    result.append(" outgoing arc(s).");
    return new String(result);
  }
} //class