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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import classycle.graph.StrongComponent;
import classycle.graph.Vertex;

/**
 * Analyze the details of class dependencies for all types in the
 * packages of an single tangle (strong component).
 * 
 * @author Frank Sauer
 *
 */
public class TangleAnalyzer {

	private StrongComponent packageTangle = null;
	private IProgressMonitor monitor = null;
	
	/**
	 * IType-handle -> {IType-handle}*
	 */
	private Map result = null;
	
	/**
	 * packageName -> {IType-handle}*
	 */
	private Map packages = null;
	
	/**
	 * The results of analyzing will be stored in the two maps given.
	 * dependencies will contain IType-handle => {IType-handle}* associations and
	 * packages will contain package-name => {IType-handle}* associations
	 * @param tangle
	 * @param dependencies
	 * @param packages
	 */
	public TangleAnalyzer(StrongComponent tangle, Map dependencies, Map packages) {
		this.packageTangle = tangle;
		this.result = dependencies;
		this.packages = packages;
	}
	
	public void analyze() {
			Display d = Display.getDefault();
			d.syncExec(new Runnable() {

				public void run() {
					try {
						new ProgressMonitorDialog(new Shell()).run(true, true,
							new IRunnableWithProgress() {
						
								public void run(IProgressMonitor m) throws InvocationTargetException, InterruptedException {
									analyze(m);
									if (m.isCanceled()) {
										result.clear();
									}
								}
						
							}
						);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
	}
	
	private void analyze(IProgressMonitor m) {
		monitor = m;
		monitor.beginTask("Analyzing tangle details", 1000);
		List packageNames = new ArrayList();
		// get all package names from the StrongComponent
		for (int i = 0; i < packageTangle.getNumberOfVertices(); i++) {
			Vertex v = packageTangle.getVertex(i);
			packageNames.add(v.getAttributes().toString());
		}
		//find corresponding IPackageFragment objects		
		List packages = getPackageFragments(packageNames);	
		if (!m.isCanceled()) getDependencies(packages);				
		monitor.done();
	}

	/**
	 * @param packages
	 * @return
	 */
	private void getDependencies(List packages) {
		try {
			SearchEngine searchEngine = new SearchEngine();
			// fill in the packageName->{IType}* map by getting all type declarations in scope
			IJavaElement[] packs = (IJavaElement[])packages.toArray(new IJavaElement[]{});
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(packs);
			SearchPattern pattern = SearchPattern.createPattern("*",IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);			
			TypeCollector c = new TypeCollector(result);
			monitor.subTask("Collecting types in packages");
			searchEngine.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, c, monitor);
			if (monitor.isCanceled()) return;
			// get all type references to these types. 
			// Have to do multiple searches to get proper relationship :-(
			Set typesInScope = result.keySet();
			monitor.worked(400);
			monitor.subTask("Collecting type dependencies");
			int scale = 500 / typesInScope.size();
			for (Iterator i = typesInScope.iterator(); i.hasNext();) {
				if (monitor.isCanceled()) return;
				String handle = (String)i.next();
				IJavaElement type = JavaCore.create(handle);
				searchEngine.searchDeclarationsOfReferencedTypes(
					type,
					new RefCollector((Set)result.get(handle), typesInScope, type),
					monitor);
				monitor.worked(scale);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param packageNames
	 * @return
	 */
	private List getPackageFragments(List packageNames) {
		monitor.subTask("Finding Packages in tangle");
		SearchEngine searchEngine = new SearchEngine();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchPattern pattern = SearchPattern.createPattern("*",IJavaSearchConstants.PACKAGE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
		PackageCollector c = new PackageCollector(packageNames);
		try {
			searchEngine.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, c, monitor);
			monitor.worked(100);
			return c.getResult();
		} catch (CoreException e) {
			e.printStackTrace();
			return new ArrayList();
		}
	}

	/**
	 * Find IPackageFragment objects from package names in component
	 */
	public class PackageCollector extends SearchRequestor {

		private List packages = new ArrayList();
		private List packageNames = null;
		
		public PackageCollector(List packageNames) {
			this.packageNames = packageNames;
		}
		
		/**
		 * 
		 */
		public List getResult() {
			return packages;			
		}

		/* count package references <em>outside</em>the current package
		 * @see org.eclipse.jdt.core.search.IJavaSearchResultCollector#accept(org.eclipse.core.resources.IResource, int, int, org.eclipse.jdt.core.IJavaElement, int)
		 */
		public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy) throws CoreException {
			if ((enclosingElement != null)&&(packageNames.contains(enclosingElement.getElementName()))) {
				packages.add(enclosingElement);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) {
			IJavaElement enclosingElement = (IJavaElement) match.getElement();
			if ((enclosingElement != null)&&(packageNames.contains(enclosingElement.getElementName()))) {
				packages.add(enclosingElement);
			}
		}
	}
	
	/**
	 * Collect all type declarations in the packages of the component
	 */
	public class TypeCollector extends SearchRequestor 
	//implements IJavaSearchResultCollector 
	{

		Map store = null;
		
		public TypeCollector(Map store) {
			this.store = store;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) {
			IJavaElement enclosingElement = (IJavaElement) match.getElement();
			try {
				if ((enclosingElement != null)&&(enclosingElement.getElementType() == IJavaElement.TYPE)) {
					String packName = enclosingElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT).getElementName();
					Set deps = new HashSet();
					store.put(enclosingElement.getHandleIdentifier(), deps);
					Set typesInPackage = (Set)packages.get(packName);
					if (typesInPackage == null) {
						typesInPackage = new HashSet();
						packages.put(packName, typesInPackage);
					}
					typesInPackage.add(enclosingElement.getHandleIdentifier());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Collect all type references in the packages of the component.
	 * Only collect references to/from classes in these packages (both to AND
	 * from must be in the scope)
	 */
	public class RefCollector extends SearchRequestor {

		Set store = null;
		Set types = null;
		IJavaElement from = null;
		IJavaElement fromPackage = null;
		
		public RefCollector(Set store, Set handles, IJavaElement from) {
			this.store = store;
			this.types = handles;
			this.from = from;
			this.fromPackage = from.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			IJavaElement enclosingElement = (IJavaElement) match.getElement();
			if ((enclosingElement != null)&&(enclosingElement.getElementType() == IJavaElement.TYPE)) {
				// found type declaration is of one of the types we want
				if (types.contains(enclosingElement.getHandleIdentifier())) {
					// it's in a different package than the from type
					//if (!fromPackage.equals(enclosingElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT))) {
						store.add(enclosingElement.getHandleIdentifier());
					//}
				}
			}
		}
	}
	
}
