/*
 * TouchGraph Software License
 *
 *
 * Copyright (c) 2001 Alexander Shapiro. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        TouchGraph (http://www.touchgraph.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "TouchGraph" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission.  For written permission, please contact
 *    alex@touchgraph.com
 *
 * 5. Products derived from this software may not be called "TouchGraph",
 *    nor may "TouchGraph" appear in their name, without prior written
 *    permission of TouchGraph.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL TOUCHGRAPH OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =====================================================================
 *
 */

package com.touchgraph.graphlayout.interaction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGPaintListener;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.TGPoint2D;

/** HVRotateDragUI.  A combination of HVScrolling + rotating.
  * The graph is rotated, but the mouse is always kept on the same point
  * on the graph.
  *
  * @author   Alexander Shapiro
  * @version  1.06
  */
public class HVRotateDragUI extends TGAbstractDragUI implements TGPaintListener {

    HVScroll hvScroll;
    RotateScroll rotateScroll;
    Node mouseOverN;
    Node tempNode;

    TGPoint2D lastMousePos;
    double lastAngle;

  // ............

   /** Constructor with TGPanel <tt>tgp</tt>, HVScroll <tt>hvs</tt>
     * and a RotateScroll <tt>rs</tt>.
     */
    public HVRotateDragUI( TGPanel tgp, HVScroll hvs, RotateScroll rs ) {
        super(tgp);
        hvScroll = hvs;
        rotateScroll = rs;
    }

    double graphDist(double x, double y) {
        double adjx=(x-this.tgPanel.getDrawCenter().getX());
        double adjy=(y-this.tgPanel.getDrawCenter().getY());
        return Math.sqrt(adjx*adjx+adjy*adjy);
    }

    double getMouseAngle(double x, double y) {
        double adjx=(x-this.tgPanel.getDrawCenter().getX());
        double adjy=(y-this.tgPanel.getDrawCenter().getY());
        double ang = Math.atan(adjy/adjx);
        if (adjx==0)
            if(adjy>0) ang=Math.PI/2;
            else ang=-Math.PI/2;
        if(adjx<0) ang=ang+Math.PI;
        return ang;
    }

    public void preActivate() {
        tgPanel.addPaintListener(this);
    }

    public void preDeactivate() {
        tgPanel.removePaintListener(this);
        tgPanel.repaint();
    }

    public void mousePressed(MouseEvent e) {
        mouseOverN=tgPanel.getMouseOverN();
        if (mouseOverN!=null) {
            lastMousePos = new TGPoint2D(mouseOverN.drawx, mouseOverN.drawy);
            lastAngle = getMouseAngle(mouseOverN.drawx, mouseOverN.drawy);
        } else {
            tempNode=new Node(); //A hack, until lenses are better implemented
                                 //One should keep track of a real position on the graph
                                 //As opposed to having a temporary node do this task.
            tempNode.drawx = e.getX();
            tempNode.drawy = e.getY();
            tgPanel.updatePosFromDraw(tempNode);
            lastMousePos = new TGPoint2D(tempNode.drawx, tempNode.drawy);
            lastAngle = getMouseAngle(tempNode.drawx, tempNode.drawy);
        }
    }

    public void mouseReleased( MouseEvent e ) {}

    public void mouseDragged( MouseEvent e ) {
        double currX = e.getX();
        double currY = e.getY();
        double currDist = graphDist(currX,currY);

        if (mouseOverN!=null)
            lastAngle = getMouseAngle(mouseOverN.drawx, mouseOverN.drawy);
        else
            lastAngle = getMouseAngle(tempNode.drawx, tempNode.drawy);

        double currentAngle = getMouseAngle(currX, currY);
        if(lastAngle>currentAngle+Math.PI) currentAngle+=Math.PI*2; //Avoids bug at x==0
        else if(currentAngle>lastAngle+Math.PI) lastAngle+=Math.PI*2;

        if (currDist>60) rotateScroll.incrementRotateAngle((currentAngle-lastAngle));

        tgPanel.updateDrawPositions(); //Rotate, but don't redraw
        tgPanel.updateGraphSize(); //Just in case.  Mostly effects H+V Scrollbars

        if(tempNode!=null) tgPanel.updateDrawPos(tempNode); //The temporary node is not part of the graph,
                                                            //So it needs to be updated individually
        TGPoint2D lastMousePos;
        if(mouseOverN!=null)
            lastMousePos = new TGPoint2D(mouseOverN.drawx, mouseOverN.drawy);
        else
            lastMousePos = new TGPoint2D(tempNode.drawx, tempNode.drawy);

        TGPoint2D newPos = new TGPoint2D(currX,currY);

        if (!hvScroll.scrolling) hvScroll.scrollAtoB(lastMousePos, newPos); //Scroll the node to the mouse

        this.tgPanel.repaintAfterMove();

        if(tempNode!=null) tgPanel.updateDrawPos(tempNode); //The temporary node is not part of the graph,
                                                            //So it needs to be updated individually
    }

    public void paintFirst( Graphics g ) {
        TGPoint2D drawCenter = tgPanel.getDrawCenter();
        g.setColor(Color.lightGray);
        for(int i=0;i<16;i++) {
            double ang = Math.PI*2*i/16;
            double rayX = 1000*Math.cos(ang);
            double rayY = 1000*Math.sin(ang);
            g.drawLine((int) drawCenter.getX(), (int) drawCenter.getY(),
                       (int) (rayX+drawCenter.getX()),(int) (rayY+drawCenter.getY()));
            g.drawLine((int) drawCenter.getX()+1, (int) drawCenter.getY(),
                       (int) (rayX+drawCenter.getX()+1),(int) (rayY+drawCenter.getY()));
            g.drawLine((int) drawCenter.getX(), (int) drawCenter.getY()+1,
                       (int) (rayX+drawCenter.getX()),(int) (rayY+drawCenter.getY()+1));
            g.drawLine((int) drawCenter.getX()+1, (int) drawCenter.getY()+1,
                       (int) (rayX+drawCenter.getX()+1),(int) (rayY+drawCenter.getY()+1));
        }


        g.fillOval((int)drawCenter.getX()-60, (int) drawCenter.getY()-60, 120,120);
        g.setColor(TGPanel.BACK_COLOR);
        g.fillOval((int)drawCenter.getX()-58, (int) drawCenter.getY()-58, 116,116);

    }

    public void paintLast( Graphics g ) {}
    public void paintAfterEdges( Graphics g ) {}

} // end com.touchgraph.graphlayout.interaction.HVRotateDragUI
