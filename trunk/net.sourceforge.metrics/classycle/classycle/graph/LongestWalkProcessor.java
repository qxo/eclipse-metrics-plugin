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



public class LongestWalkProcessor extends GraphProcessor
{
  protected void initializeProcessing(Vertex[] graph)
  {
  }

  protected void processBefore(Vertex vertex)
  {
    StrongComponent component = castAsStrongComponent(vertex);
    component.setActive(true);
    component.setLongestWalk(0);
  }

  protected void processArc(Vertex tail, Vertex head)
  {
    StrongComponent t = castAsStrongComponent(tail);
    StrongComponent h = castAsStrongComponent(head);
    if (!h.isVisited())
    {
      process(h);
    }
    else if (h.isActive())
    {
      // Oops! should never be happen if the graph has been created
      // with StrongComponentProcessor
      throw new IllegalArgumentException(h + " is not a strong component.");
    }
    t.setLongestWalk(Math.max(t.getLongestWalk(), 1 + h.getLongestWalk()));
  }

  protected void processAfter(Vertex vertex)
  {
    castAsStrongComponent(vertex).setActive(false);
  }

  protected void finishProcessing(Vertex[] graph)
  {
    // Sort in increasing walk lengths
    StrongComponent[] components = (StrongComponent[]) graph;
    for (int i = 0; i < graph.length; i++)
    {
      for (int j = i + 1; j < graph.length; j++)
      {
        if (components[j].getLongestWalk() < components[i].getLongestWalk())
        {
          StrongComponent c = components[i];
          components[i] = components[j];
          components[j] = c;
        }
      }
    }
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
  }} //class