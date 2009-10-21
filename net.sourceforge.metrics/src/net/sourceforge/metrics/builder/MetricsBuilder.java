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
 * $Id: MetricsBuilder.java,v 1.26 2004/05/29 03:41:53 sauerf Exp $
 */
 
package net.sourceforge.metrics.builder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Cache;
import net.sourceforge.metrics.core.sources.Dispatcher;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * builder to (re)calculate metrics for modified java resources.
 * 
 * @author Frank Sauer
 */
public class MetricsBuilder extends IncrementalProjectBuilder {
	
	private static Queue queue    = new Queue();
	private static CalculatorThread thread = null;
	private static ProgressQueue notifier = new ProgressQueue(queue);
	
	private static Set currentProjects = new HashSet();
	
	private static Boolean headless = null;
	
	public MetricsBuilder() {		
		super();
	}

	public static void addMetricsProgressListener(IMetricsProgressListener l) {
		notifier.addMetricsProgressListener(l);
	}
	
	public static void removeMetricsProgressListener(IMetricsProgressListener l) {
		notifier.removeMetricsProgressListener(l);
	}
	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		try {
			if (hasErrors(getProject())) return null;
			checkCancel(monitor);
			IJavaProject currentProject = JavaCore.create(getProject());
			if (currentProject == null) return null;
			//Log.logMessage("New build started for " + currentProject.getElementName());
			startCalculatorThread();
			if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			    fullBuild(currentProject, monitor);
			} else {
				// check for a previously completed full build
				AbstractMetricSource p = Cache.singleton.get(currentProject);
				if (p == null) {
					fullBuild(currentProject, monitor);
				} else {
				    IResourceDelta delta = getDelta(getProject());
				    incrementalBuild(delta, monitor);
				}
			}
		} catch (OperationCanceledException x) {
			throw x;
		} catch (Throwable e) {
			Log.logError("Error in MetricsBuilder", e);
		}
		return null;
	}

	/**
	 * determine if project has compilation errors
	 * @param project
	 * @return true if project has compile errors
	 */
	private boolean hasErrors(IProject project) {
		try {
			IMarker[] markerList = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if (markerList == null || markerList.length == 0)	return false;
			IMarker marker = null;
			int numErrors = 0;
			for (int i = 0; i < markerList.length; i++) {
				marker = markerList[i];
				int severity =	marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				if (severity == IMarker.SEVERITY_ERROR) {
					numErrors++;
				}
			}
			return numErrors>0;
		} catch (CoreException e) {
			Log.logError("CoreException getting build errors: " , e);
			return false;
		}
	}

	private static void startCalculatorThread() {
		if (thread == null) {
			thread = new CalculatorThread();
			thread.start();
		}
	}
	
	/**
	 * Answers true if the commandline that started eclipse contained "-noupdate"
	 * This is used to determine whether to calculate metrics in the background
	 * (normal operation in UI) or in the foreground (headless operation from Ant)
	 * @return true if running headless, false when in UI mode
	 */
	private static boolean isHeadless() {
		if (headless == null) {
			headless = Boolean.FALSE;
			String[] args = Platform.getCommandLineArgs();
			for (int i = 0; i < args.length; i++) {
				if ("-noupdate".equals(args[i])) {
					headless = Boolean.TRUE;
					break;
				}
			} 
		}
		return headless.booleanValue();
	}
	
	/**
	 * Do a full build and recalculate metrics for all java resources in the project
	 * @param monitor
	 * @throws CoreException
	 */
	protected void fullBuild(IJavaProject currentProject, IProgressMonitor monitor) throws CoreException {
   		MetricsBuildVisitor v = new MetricsBuildVisitor(monitor);
   		Cache.singleton.clear(currentProject.getElementName());
	  	getProject().accept(v);
		checkCancel(monitor);
		v.execute();
	}
	
	/**
	 * recalculate metrics for changed/added/removed java resources
	 * @param delta
	 * @param monitor
	 * @throws CoreException
	 */
	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		if (delta != null) {
			// this is a hack to avoid a build on enable only, not working
			if (!isHeadless() && monitor instanceof NullProgressMonitor) return;
			MetricsBuildVisitor v = new MetricsBuildVisitor(monitor);
		    delta.accept(v);
			checkCancel(monitor);
		    v.execute();
		}
	}

	/**
	 * Contains the result of the resource to IJavaElement translation/filtering
	 * and knows how to process it in the context of a full build as well as an 
	 * incremental build
	 *  
	 * @author Frank Sauer
	 */
	class FilterResult {
		public IPackageFragmentRoot defaultSourceFolder;
		IJavaElement element;
		boolean processChildren;
		IPackageFragment defaultPackage;
		
		boolean process(Stack stack) {
			if (element != null) {
				pushChangedCommands(stack);
				return true;
			} else return processChildren;
		}
		
		/**
		 * Push one, two or three ChangedCommand objects onto the stack.
		 * One is the normal case. Two if this result holds a source folder
		 * with a default package. Three if this result holds a project with
		 * no source folders and a default package (BUG #766261) 
		 * @param stack
		 */
		private void pushChangedCommands(Stack stack) {
			stack.push(new ChangedCommand(element));
			if (defaultSourceFolder != null) {
				stack.push(new ChangedCommand(defaultSourceFolder));
			}
			if (defaultPackage != null) {
				stack.push(new ChangedCommand(defaultPackage));
			}
		}	
		
		boolean process(Stack stack, IResourceDelta delta) {
			if (element == null) return processChildren;
			switch (delta.getKind()) {
				case IResourceDelta.ADDED : 
					AddedCommand added = new AddedCommand(element);
					IPath from = delta.getMovedFromPath();
					if ( from != null) {
						added.setMovedFromPath(from);
					}
					stack.push(added);
					break;
				case IResourceDelta.REMOVED:
					stack.push(new RemovedCommand(element));
					break;
				case IResourceDelta.CHANGED:
					pushChangedCommands(stack);
					break;
			}
			return true;
		}			
	}
	
	/**
	 * implements both the ResourceVisitor for a full build and the DeltaVisitor for
	 * an incremental build. pushes commands on a stack so that they get executed in
	 * a depth first order.
	 * 
	 * @author Frank Sauer
	 */
	class MetricsBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {
		
	   private Stack stack = new Stack();
	   private IProgressMonitor monitor;
	   
	   MetricsBuildVisitor(IProgressMonitor monitor) {
	   		this.monitor = monitor;
	   }
	   
	   /**
	    * pushes ChangedCommand for all resources in a project (full build)
	    */
	   public boolean visit(IResource res) {
			checkCancel(monitor);
			FilterResult result = filter(res);
			return result.process(stack);
	   }
	   
	   /**
	    * filters and translates the resource thrown at it from the builder
	    * @param res
	    * @return FilterResult
	    */
	   private FilterResult filter(IResource resource) {
			FilterResult result = new FilterResult();
	   		result.processChildren = false;
	   		if (resource == null) return result;
			IJavaElement element = JavaCore.create(resource);
			if (element == null) {
				// deal with high-level intermediate non-sourcefolders
				// that occur when linking in source folders from outside
				// the workspace or other bizar projects, skip the folder
				// but process its children...
				if (resource.getType() == IResource.FOLDER) 
					result.processChildren = true;
				return result;
			}
			
			// same thing shows up once in classes, once in src...
			if (stack.contains(element)) return result;
			
			// skip jars/zips
			if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				IPackageFragmentRoot candidate = (IPackageFragmentRoot)element;
				if (candidate.isArchive()) return result;
			}
			
			// check for default package in a source folder
			if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				IPackageFragmentRoot candidate = (IPackageFragmentRoot)element;
				IPackageFragment defPackage = candidate.getPackageFragment("");
				try {
					if ((defPackage != null)&&(defPackage.hasChildren()))
						result.defaultPackage = defPackage;
				} catch (JavaModelException e) {
				}
			}
			
			// check for a default package in a project (BUG #766261)
		    if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
				IJavaProject p = (IJavaProject)element;
				IPackageFragmentRoot r = p.getPackageFragmentRoot(resource);
				if (r.exists()) { // BUG #931018 
					// PackageFragmentRoot == JavaProject (same resource)!!!					
					result.defaultSourceFolder = r;
					IPackageFragment dp = r.getPackageFragment("");
					if (dp != null) {
						result.defaultPackage = dp;
					}
				}
		    }
		    
			// skip java files in regular (non-source) folders
			if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
					try {
						element.getUnderlyingResource();
					} catch (JavaModelException e) {
						return result;
					}
			}
			
			// skip class files
			if (element.getElementType() == IJavaElement.CLASS_FILE) return result;
			
			// skip empty parent packages and binary packages
			if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				IPackageFragment pack = (IPackageFragment)element;
				try {
					result.processChildren = pack.getKind()==IPackageFragmentRoot.K_SOURCE;
					if ((!result.processChildren)||(pack.getCompilationUnits().length == 0)) {
						return result;
					}
				} catch (JavaModelException e) {
					return result;
				}
			}
			result.element = element;
			return result;
	   }
	   
	   /**
	    * determines the kind of command needed and pushes it on the stack.
	    * Used by incremental builds
	    */
	   public boolean visit(IResourceDelta delta) {
			checkCancel(monitor);
	   		FilterResult result = filter(delta.getResource());
	   		return result.process(stack, delta);	   		
	   }
	   
	   /**
	    * Queue commands in UI mode or execute them immediately in headless mode
	    *
	    */
	   public void execute() {
		   	if (isHeadless()) {
		   		executeHeadless();
		   	} else {
		   		executeUI();
		   	}
	   }
	   
	    /**
		 * execute commands in the foreground in headless mode so Ant task waits for completion
		 */
		private void executeHeadless() {
			while (stack.size()>0) {
				Command next = (Command) stack.pop();
				if (monitor != null) monitor.subTask("Calculating metrics for " + next.getElement().getElementName());
				try {
					next.execute();
				} catch (Throwable t) {
					Log.logError("(headless) error calculating metrics for " + next.getHandleIdentifier(), t);
				}
			}
		}

    	/**
	    * Executes all commands on the stack by popping them off until empty
	    * fires progress events (pending and completed to listeners)
	    */
	   private void executeUI() {
	   		int count = 0;
	   		// make sure we fire the queued event before any pending events
	   		synchronized(queue) {
				try {
					while (stack.size()>0) {
						checkCancel(monitor);
						Command next = (Command) stack.pop();
						monitor.subTask("Queuing " + next.getElement().getElementName());
						if (next.getElement().getElementType() == IJavaElement.JAVA_PROJECT) {
							synchronized(currentProjects) {
								currentProjects.add(next.getHandleIdentifier());
							}
						}
						if (queue.queue(next)) count++;
					}
					if (count>0) notifier.fireQueued(count);
				} catch (OperationCanceledException e) {
					Log.logMessage("Metrics queuing aborted by user.");
					stack.clear();
					queue.clear();
					throw e;
				}
	   		}
	   }
	   
	}
	
	/**
	 * Base class for the commands
	 * @author Frank Sauer
	 */
	static abstract class Command {
		
		protected IJavaElement element = null;
		protected Object result = null;
		
		public Command(IJavaElement element) {
			this.element = element;
		}
		
		public String getHandleIdentifier() {
			return element.getHandleIdentifier();
		}
		
		public IJavaElement getElement() {
			return element;
		}
		
		public int hashCode() {
			return element.getHandleIdentifier().hashCode();
		}
		
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof String) return element.getHandleIdentifier().equals(o);
			if (o instanceof Command) return element.equals(((Command)o).element);
			return false;
		}
		
		abstract void execute();
		
		public Object getResult() {
			return result;
		}
		
		protected void setResult(Object o) {
			result = o;
		}
		
		public IPath getMovedFrom() {
			return null;
		}
		
		public void removeMetricsFromCache() {
			if (element.getElementType() == IJavaElement.COMPILATION_UNIT)
				Cache.singleton.removeSubtree(element.getHandleIdentifier());
			else
				Cache.singleton.remove(element.getHandleIdentifier());
		}
		
		public String toString() {
			return element.getElementName();
		}
	}
	
	/**
	 * ChangedCommand removes cached metricas and recalculates them 
	 * @author Frank Sauer
	 */
	static class ChangedCommand extends Command {

		ChangedCommand(IJavaElement element) {
			super(element);
		}

		void execute() {
			removeMetricsFromCache();
			setResult(Dispatcher.calculateAbstractMetricSource(element));
		}
		
	}
	
	/**
	 * AddedCommand calculates the metrics for a new/moved resource 
	 * @author Frank Sauer
	 */
	static class AddedCommand extends Command {

		private IPath movedFrom;

		AddedCommand(IJavaElement element) {
			super(element);
		}

		/**
		 * @param from
		 */
		public void setMovedFromPath(IPath from) {
			this.movedFrom = from;
			
		}

		void execute() {
			removeMetricsFromCache();
			setResult(Dispatcher.calculateAbstractMetricSource(element));
		}
		
		public IPath getMovedFrom() {
			return movedFrom;
		}
	}	
	
	/**
	 * RemovedCommand removes the cached metrics 
	 * @author Frank Sauer
	 */
	static class RemovedCommand extends Command {

		RemovedCommand(IJavaElement element) {
			super(element);
		}

		void execute() {
			removeMetricsFromCache();
		}
		
	}		
	
	public static class Queue extends LinkedList {
		private Semaphore sem = new Semaphore(0);
		
		/**
		 * insert command before its nearest ancestor in the queue
		 * if found, otherwise, add to the end. This avoids duplicate
		 * work.
		 * @param command
		 */
		public boolean queue(Command command) {
			synchronized(this) {
				// skip new command if it is already queued by a previous build
				if (contains(command)) return false;
				// insert it before nearest ancestor or at end
				int ancestor = findAncestorIndex(command);
				if (ancestor == -1) {
					addLast(command);
				} else {					
					add(ancestor, command);
				}
			}
			sem.V();
			return true;
		}
		
		private int findAncestorIndex(Command command) {
			int ancestor = -1;
			int index = 0;
			for (Iterator i = iterator(); i.hasNext();index++) {
				Command next = (Command)i.next();
				if (command.getHandleIdentifier().startsWith(next.getHandleIdentifier())) {
					ancestor = index;
					break;
				}
			}
			return ancestor;
		}
		
		public Command dequeue() throws InterruptedException {
			sem.P();
			synchronized(this) {
				return (Command)super.removeFirst();
			}
		}		
		
		public void clear() {
			sem.reset();
			synchronized(this) {
				super.clear();
			}
		}
		
		/**
		 * remove all command with a handle starting with the given projectHandle
		 * @param projectHandle
		 */
		public int removeAll(String projectHandle) {
			synchronized(this) {
				int count = 0;
				for (Iterator i = iterator();i.hasNext();) {
					Command next = (Command)i.next();
					if (next.getHandleIdentifier().startsWith(projectHandle)) {
						i.remove();
						count++;
					}
				}
				int leftOver = size();
				//Log.logMessage("Removed " + count + ". still queued: " + leftOver);
				sem.reset(leftOver);
				return leftOver;
			}
		}
	}
	
	/**
	 * abort any calculations for the given project only, other will complete
	 * @param projectHandle
	 */
	public static void abort(String projectHandle) {		
		int leftOver = queue.removeAll(projectHandle);
		if (thread != null) thread.abort(projectHandle);
		IJavaProject p = (IJavaProject) JavaCore.create(projectHandle);
		notifier.fireProjectCompleted(p,true);
		currentProjects.remove(projectHandle);
		if (leftOver > 0) {
			notifier.fireQueued(leftOver);
			startCalculatorThread();
		}
	}
	
	/**
	 * kill all ongoing calculations and remove pending ones from the queue.
	 */
	public static void abortAll() {
		for (Iterator i = currentProjects.iterator(); i.hasNext();) {
			if (thread != null) thread.abort((String)i.next());
		}	
		queue.clear();
		notifier.fireProjectCompleted(null,true);
		currentProjects.clear();
	}
	
	public static boolean isBuilding(String projectHandle) {
		synchronized(currentProjects) {
			return currentProjects.contains(projectHandle);
		}
	}
	
	public static void pause() {
		startCalculatorThread(); // make sure we have one to pause
		if (thread != null) {
			thread.pause(true);
		}
	}
	
	public static void resume() {
		if (thread != null) thread.pause(false);
	}
	
	public static boolean canPause() {
		return thread == null || !thread.paused;
	}
	
	public static boolean canResume() {
		return thread != null && thread.paused;
	}
	
	public static boolean canAbort() {
		return thread != null && queue.size()>0;
	}
	
	private static Object Pause = new Object(); // pause/resume semaphore
	
	public static class CalculatorThread extends Thread {
		
		private Command current;
		private boolean paused = false;
		
		public CalculatorThread() {
			super("Metrics Calculator Thread");
		}
				
		public void run() {
			try {
				//Log.logMessage("New Calculator Thread is born...");
				while (thread == Thread.currentThread()) {
					checkPaused();
					current = queue.dequeue(); //blocks!
					checkPaused();
					if (!Thread.currentThread().isInterrupted()) {
						IJavaElement currentElm = current.getElement();
						//Log.logMessage("Executing " + current.getHandleIdentifier());
						notifier.firePending(currentElm);
						current.execute();
						// only notify if we weren't aborted
						if (!Thread.currentThread().isInterrupted()) {
							if (current.getMovedFrom() != null)
								notifier.fireMoved(currentElm, current.getMovedFrom());
							notifier.fireCompleted(currentElm, current.getResult());
							if (currentElm.getElementType() == IJavaElement.JAVA_PROJECT) {
								synchronized(currentProjects) {
									currentProjects.remove(currentElm.getHandleIdentifier());
								}
								notifier.fireProjectCompleted((IJavaProject)currentElm, false);
							}
						} 
					}
				}
			} catch (InterruptedException e) {
				//Log.logMessage("Interrupted!");
			} catch (Throwable t) {
				Log.logError("CalculatorThread terminated.", t);
			} finally {
				// make sure a new thread is created next time around
				thread = null;
			}
		}

		private void checkPaused() throws InterruptedException {
			if (paused) notifier.firePaused();
			synchronized(Pause) {
				while (paused) Pause.wait();
			}
		}

		public void pause(boolean pause) {
			paused = pause;
			if (!paused)
				synchronized(Pause) { 
					Pause.notify();
				}
		}
		
		public void abort(String projectHandle) {
			if (current == null) return;
			if (current.getHandleIdentifier().startsWith(projectHandle)) {
				//Log.logMessage("Going to interrrupt current calculation.");
				interrupt();
			}
		}
	}
	
}
