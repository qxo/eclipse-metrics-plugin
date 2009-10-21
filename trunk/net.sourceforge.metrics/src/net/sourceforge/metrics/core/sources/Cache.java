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
package net.sourceforge.metrics.core.sources;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.helper.FastIterator;
import jdbm.helper.IterationException;
import jdbm.htree.HTree;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

/**
 * public API to the private database. Currently the database is a jdbm persistent hashtable with MRU cache.
 * 
 * @author Frank Sauer
 */
public class Cache {

	private static final String DBNAME = "/metricsdb";
	private RecordManager recman;

	private String pluginDir;

	public final static Cache singleton = new Cache();

	// keep roots (projectName -> HTree)
	private Map<String, HTree> projects = new HashMap<String, HTree>();
	private Map<String, Set<String>> keys = new HashMap<String, Set<String>>();

	private Cache() {
		super();
		// the follwing fixes a bug submitted outside of SF by Parasoft
		pluginDir = MetricsPlugin.getDefault().getStateLocation().toString();
		// pluginDir =
		// Platform.getPlugin(Log.pluginId).getStateLocation().toString();
		initRecordManager();
	}

	private void initRecordManager() {
		try {
			Properties props = new Properties();
			props.put(RecordManagerOptions.CACHE_SIZE, "500");
			props.put(RecordManagerOptions.AUTO_COMMIT, "false");
			props.put(RecordManagerOptions.THREAD_SAFE, "true");
			recman = RecordManagerFactory.createRecordManager(pluginDir + DBNAME, props);
		} catch (Throwable e) {
			Log.logError("Could not open/create jdbm database", e);
		}
	}

	private HTree getHashtableForProject(String projectName) {
		HTree hashtable = projects.get(projectName);
		if (hashtable == null) {
			try {
				long recid = recman.getNamedObject(projectName);
				if (recid != 0) {
					hashtable = HTree.load(recman, recid);
				} else {
					hashtable = HTree.createInstance(recman);
					recman.setNamedObject(projectName, hashtable.getRecid());
				}
				projects.put(projectName, hashtable);
			} catch (Throwable e) {
				Log.logError("Could not get/create HTree for " + projectName, e);
			}
		}
		return hashtable;
	}

	private HTree getHashtableForHandle(String handle) {
		IJavaElement element = JavaCore.create(handle);
		String projectName = getProjectName(element);
		return getHashtableForProject(projectName);
	}

	/**
	 * @param element
	 * @return
	 */
	private String getProjectName(IJavaElement element) {
		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			return element.getElementName();
		} /* else { */
		IJavaElement p = element.getAncestor(IJavaElement.JAVA_PROJECT);
		return p.getElementName();
		/* } */
	}

	public void put(AbstractMetricSource source) {
		if (source == null) {
			return;
		}
		try {
			String handle = source.getHandle();
			getHashtableForHandle(handle).put(handle, source);
			getKeysForHandle(handle).add(handle);
			if (source.getLevel() >= Constants.PACKAGEFRAGMENT) {
				recman.commit();
			}
		} catch (Throwable e) {
			Log.logError("Could not store " + source.getHandle(), e);
		}
	}

	/**
	 * @param handle
	 */
	public Set<String> getKeysForHandle(String handle) {
		IJavaElement element = JavaCore.create(handle);
		String projectName = getProjectName(element);
		Set<String> s = keys.get(projectName);
		if (s == null) {
			s = getKeys(handle);
			keys.put(projectName, s);
		}
		return s;

	}

	private Set<String> getKeys(String handle) {
		HTree map = getHashtableForHandle(handle);
		Set<String> result = new HashSet<String>();
		try {
			FastIterator it = map.keys();
			String next = (String) it.next();
			while (next != null) {
				result.add(next);
				next = (String) it.next();
			}
		} catch (IterationException e) {
			// ok
		} catch (Throwable e) {
			Log.logError("Error iterating over database keys", e);
		}

		return result;
	}

	public AbstractMetricSource get(IJavaElement element) {
		return get(element.getHandleIdentifier());
	}

	public AbstractMetricSource get(String handle) {
		try {
			return (AbstractMetricSource) getHashtableForHandle(handle).get(handle);
		} catch (Throwable e) {
			Log.logError("Error fetching data for " + handle, e);
			return null;
		}
	}

	public void remove(String handle) {
		try {
			getHashtableForHandle(handle).remove(handle);
			getKeysForHandle(handle).remove(handle);
		} catch (Throwable e) {
			Log.logError("Could not remove " + handle, e);
		}
	}

	public void removeSubtree(String handle) {
		HTree h = getHashtableForHandle(handle);
		if (h != null) {
			Set handles = getKeysForHandle(handle);
			for (Iterator i = handles.iterator(); i.hasNext();) {
				String next = (String) i.next();
				if (next.startsWith(handle)) {
					try {
						h.remove(next);
						i.remove();
					} catch (Throwable e) {
						// doesn't seem to be a severe problem, don't log
						Log.logError("Could not remove " + next, e);
					}
				}
			}
		}
	}

	public void close() {
		try {
			recman.close();
			keys.clear();
			projects.clear();
		} catch (Throwable e) {
			Log.logError("Could not close jdbm database", e);
		}
	}

	/**
	 * permanently remove all metrics related to given project
	 * 
	 * @param projectName
	 */
	public void clear(String projectName) {
		try {
			keys.remove(projectName);
			long id = recman.getNamedObject(projectName);
			if (id != 0) {
				recman.delete(id);
				HTree hashtable = HTree.createInstance(recman);
				recman.setNamedObject(projectName, hashtable.getRecid());
				recman.commit();
			}
		} catch (Throwable e) {
			Log.logError("Could not clear project " + projectName, e);
		}
	}

	/**
	 * clean out entire database
	 */
	public void clear() {
		try {
			recman.close();
			File db = new File(pluginDir + DBNAME);
			db.delete();
			initRecordManager();
			keys.clear();
		} catch (Throwable e) {
			Log.logError("Error deleting database", e);
		}

	}

	/**
	 * 
	 */
	public void commit() {
		try {
			recman.commit();
		} catch (Throwable e) {
			Log.logError("Could not commit latest changes.", e);
		}

	}
}
