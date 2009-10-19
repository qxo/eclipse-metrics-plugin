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
 * $Id: Semaphore.java,v 1.3 2003/06/11 13:23:36 sauerf Exp $
 */
package net.sourceforge.metrics.builder;


/**
 * Simple counting semaphore for queue blocking
 * 
 * @author Frank Sauer
 */
public class Semaphore {
	private int count = 0;
	private int initial = 0;
	
	public Semaphore(int initialCount) {
		count = initialCount;
		initial = initialCount;
	}
	
	public synchronized void P() throws InterruptedException {
		while (count <= 0) {
			wait();
		}
		count--;
	}
	
	public synchronized void V() {
		++count;
		notify();
	}
	
	public synchronized void reset() {
		count = initial;
	}
	
	public synchronized void reset(int value) {
		count = value;
	}
}