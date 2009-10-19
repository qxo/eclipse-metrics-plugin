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
 * $Id: ProgressQueue.java,v 1.7 2003/07/03 04:05:10 sauerf Exp $
 */
 package net.sourceforge.metrics.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.metrics.core.Log;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Decouples the calculation thread from the others. Internal use only
 * @author Frank Sauer
 */
public class ProgressQueue extends LinkedList {

	private boolean paused;
	private Collection items;
	private List listeners = new ArrayList();
	private Semaphore sem = new Semaphore(0);
	private Thread notifier = new NotifierThread();
	
	public ProgressQueue(Collection c) {
		this.items = c;
		notifier.start();
	}
	
	/**
	 * Add listener l to be notified of metrics progress.
	 * If there are any calculations already ongoing, l will be caught up
	 * by the immediate receipt on the current thread of a queued event
	 * with the current number of outstanding calculations.
	 * @param l
	 */
	public void addMetricsProgressListener(IMetricsProgressListener l) {
		if ((l != null)&&(!listeners.contains(l))) {
			listeners.add(l);
			synchronized(items) {
				if (items.size()>0) {
					Log.logMessage("Catching up new metrics progress listener with " + items.size() + " items.");
					// play catch up with l
					l.queued(items.size());
				}
			}
		}
	}
	
	public void removeMetricsProgressListener(IMetricsProgressListener l) {
		if (l != null) listeners.remove(l);
	}
	
	public void firePending(IJavaElement element) {
		queue(new PendingCommand(element));
	}
	
	public void fireCompleted(IJavaElement element, Object data) {
		queue(new CompletedCommand(element, data));
	}
	
	public void fireProjectCompleted(IJavaProject project, boolean aborted) {
		if (aborted) {
			clear();
			new ProjectCompleteCommand(project, aborted).execute();
		} else queue(new ProjectCompleteCommand(project, aborted));
	}
	
	public void firePaused() {
		queue(new PausedCommand());
	}

	public void fireMoved(IJavaElement element, IPath from) {		
		queue(new MovedCommand(element, from));
	}

	public void fireQueued(int count) {
		queue(new QueuedCommand(count));
	}
	
	/**
	 * Queue the command unless queue is paused and the command is not
	 * a command that triggers resume (QueueCommand)
	 * @param command
	 */
	private void queue(Command command) {
		synchronized(this) {
			if (command.isResume()) paused = false;
			if (!paused) addLast(command);				
		}
		sem.V();
	}
	
	/**
	 * Get the first queued command. Pauses the queue if this command
	 * is a pausing command (AbortCommand). Throws InterruptedException
	 * if the Queue is blocked on its Semaphore and the thread was interrupted.
	 * @return Command to be executed
	 * @throws InterruptedException
	 */
	private Command dequeue() throws InterruptedException {
		sem.P();
		synchronized(this) {
			Command c = (Command)super.removeFirst();
			if (c.isPause()) paused = true;
			return c;
		}
	}		
	
	/**
	 * This Thread delivers the notification events to the listeners
	 * @author Frank Sauer
	 */
	private class NotifierThread extends Thread {
		
		public NotifierThread() {
			super("Metrics Notifier Thread");
		}
		
		public void run() {
			try {
				while (notifier == Thread.currentThread()) {
					Command next = dequeue(); //blocks!
					try {
						next.execute();
					} catch (Throwable e) {
						Log.logError("uncaught exception in metrics notifier", e);
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}	
	
	private abstract class Command {
		
		public Command() {
		}
		
		abstract void execute();
		
		boolean isResume() {
			return false;
		}	
		
		boolean isPause() {
			return false;
		}
	}
	
	private class PendingCommand extends Command {

		private IJavaElement element;

		PendingCommand(IJavaElement element) {
			this.element = element;
		}

		void execute() {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IMetricsProgressListener next = (IMetricsProgressListener)i.next();
				next.pending(element);
			}
		}
	}
	
	private class CompletedCommand extends Command {

		private Object data;

		private IJavaElement element;

		CompletedCommand(IJavaElement element, Object data) {
			this.element = element;
			this.data = data;
		}

		void execute() {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IMetricsProgressListener next = (IMetricsProgressListener)i.next();
				next.completed(element, data);
			}
		}
	}
	
	private class MovedCommand extends Command {

		private IPath path;

		private IJavaElement element;

		MovedCommand(IJavaElement element, IPath path) {
			this.element = element;
			this.path = path;
		}

		void execute() {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IMetricsProgressListener next = (IMetricsProgressListener)i.next();
				next.moved(element, path);
			}
		}
	}
	
	private class PausedCommand extends Command {

		PausedCommand() {
		}

		void execute() {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IMetricsProgressListener next = (IMetricsProgressListener)i.next();
				next.paused();
			}
		}		
	}	
	
	private class QueuedCommand extends Command {

		private int count;

		QueuedCommand(int count) {
			this.count = count;
		}

		void execute() {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IMetricsProgressListener next = (IMetricsProgressListener)i.next();
				next.queued(count);
			}
		}
		
		boolean isResume() {
			return true;
		}
	}	
	
	private class ProjectCompleteCommand extends Command {
		private boolean aborted = false;
		IJavaProject project;
		
		ProjectCompleteCommand(IJavaProject project, boolean aborted) {
			this.aborted = aborted;
			this.project = project;
		}

		void execute() {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IMetricsProgressListener next = (IMetricsProgressListener)i.next();
				next.projectComplete(project, aborted);
			}
		}
	}
	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		synchronized(this) {
			super.clear();
			sem.reset();
		}
	}

}
