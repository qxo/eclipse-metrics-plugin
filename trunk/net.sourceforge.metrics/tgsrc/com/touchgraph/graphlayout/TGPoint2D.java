/*
 * TouchGraph LLC. Apache-Style Software License
 *
 *
 * Copyright (c) 2001-2002 Alexander Shapiro. All rights reserved.
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
 *        TouchGraph LLC (http://www.touchgraph.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "TouchGraph" or "TouchGraph LLC" must not be used to endorse 
 *    or promote products derived from this software without prior written 
 *    permission.  For written permission, please contact 
 *    alex@touchgraph.com
 *
 * 5. Products derived from this software may not be called "TouchGraph",
 *    nor may "TouchGraph" appear in their name, without prior written
 *    permission of alex@touchgraph.com.
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
 * ====================================================================
 *
 */

package com.touchgraph.graphlayout;

import net.sourceforge.metrics.core.Log;

/**
 * TGPoint2D is only needed for java 1.1.
 * 
 * @author Alexander Shapiro
 * @version 1.22-jre1.1 $Id: TGPoint2D.java,v 1.2 2004/10/25 06:57:41 donv70 Exp $
 */
public class TGPoint2D {

	private double x;
	private double y;

	public TGPoint2D(double xpos, double ypos) {
		setX(xpos);
		setY(ypos);
	}

	public TGPoint2D(TGPoint2D p) {
		setX(p.getX());
		setY(p.getY());
	}

	public void setLocation(double xpos, double ypos) {
		setX(xpos);
		setY(ypos);
	}

	public void setX(double newX) {
		x = newX;
		if (Double.isInfinite(newX)) {
			x = newX > 0 ? Double.MAX_VALUE : Double.MIN_VALUE;
		}
		if (Double.isNaN(newX) || newX == Double.NaN) {
			Log.logMessage("Argh!");
		}
	}

	public double getX() {
		return x;
	}

	public void setY(double newY) {
		y = newY;
		if (Double.isInfinite(newY)) {
			y = newY > 0 ? Double.MAX_VALUE : Double.MIN_VALUE;
		}
		if (Double.isNaN(newY) || Double.isInfinite(newY) || newY == Double.NaN) {
			Log.logMessage("Argh!");
		}

	}

	public double getY() {
		return y;
	}

} // end com.touchgraph.graphlayout.TGPoint2D
