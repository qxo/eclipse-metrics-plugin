/*
 * Copyright (c) 2003 Frank Sauer. All rights reserved. Licenced under CPL 1.0
 * (Common Public License Version 1.0). The licence is available at
 * http://www.eclipse.org/legal/cpl-v10.html. DISCLAIMER OF WARRANTIES AND
 * LIABILITY: THE SOFTWARE IS PROVIDED "AS IS". THE AUTHOR MAKES NO
 * REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS OR IMPLIED. TO THE EXTENT NOT
 * PROHIBITED BY LAW, IN NO EVENT WILL THE AUTHOR BE LIABLE FOR ANY DAMAGES,
 * INCLUDING WITHOUT LIMITATION, LOST REVENUE, PROFITS OR DATA, OR FOR SPECIAL,
 * INDIRECT, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO ANY
 * FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE
 * AUTHOR HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. $id$
 */
package net.sourceforge.metrics.ui.layeredpackagegraph;

import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;


/**
 * view actions for the MetricsView (Recalculate and Export XML)
 * 
 * @author Frank Sauer
 */
public class LayeredPackageGraphActionGroup extends ActionGroup {
    private ResumeAction resumeAction;
    private PauseAction pauseAction;
    private ExportAction exportAction;
    private GraphAction graphAction;
    private AbortAllAction abortAction;
    private LayeredPackageTableView metricsView;

    public LayeredPackageGraphActionGroup(LayeredPackageTableView view) {
        this.metricsView = view;
        createActions();
    }

    /**
     * @param view
     */
    private void createActions() {
        exportAction = new ExportAction();
        graphAction = new GraphAction();
        abortAction = new AbortAllAction();
        pauseAction = new PauseAction();
        resumeAction = new ResumeAction();
    }

    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        fillToolBar(actionBars.getToolBarManager());
        fillViewMenu(actionBars.getMenuManager());
    }

    void fillToolBar(IToolBarManager toolBar) {
        toolBar.removeAll();
        toolBar.add(resumeAction);
        toolBar.add(pauseAction);
        toolBar.add(abortAction);
        toolBar.add(exportAction);
        toolBar.add(graphAction);
    }

    void fillViewMenu(IMenuManager menu) {
        menu.add(resumeAction);
        menu.add(pauseAction);
        menu.add(abortAction);
        menu.add(exportAction);
        menu.add(graphAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
    }

    private class GraphAction extends Action {
        public GraphAction() {
            super("&Dependency Graph", MetricsPlugin.createImage("gview.gif"));
            setToolTipText("&Open the Dependency Graph View");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                getView().displayDependencyGraph();
            } catch (RuntimeException e) {
                Log.logError("GraphAction::run", e);
            }
        }
    }

    private class ExportAction extends Action {
        public ExportAction() {
            super("&Export XML...", MetricsPlugin.createImage("export_xml.gif"));
            setToolTipText("&Export XML...");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                getView().exportXML();
            } catch (RuntimeException e) {
                Log.logError("ExportAction::run", e);
            }
        }
    }

    private class AbortAllAction extends Action {
        public AbortAllAction() {
            super("&Abort All Calculations...", MetricsPlugin
                    .createImage("abort.gif"));
            setToolTipText("&Abort all ongoing and pending calculations");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                MetricsBuilder.abortAll();
            } catch (RuntimeException e) {
                Log.logError("AbortAllAction::run", e);
            }
        }
    }

    private class PauseAction extends Action {
        public PauseAction() {
            super("&Pause Calculations...", MetricsPlugin
                    .createImage("pause.gif"));
            setToolTipText("&Temporarily pause all calculations");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                MetricsBuilder.pause();
                pauseAction.setEnabled(MetricsBuilder.canPause());
                resumeAction.setEnabled(MetricsBuilder.canResume());
            } catch (RuntimeException e) {
                Log.logError("PauseAction::run", e);
            }
        }
    }

    private class ResumeAction extends Action {
        public ResumeAction() {
            super("&Resume Calculations...", MetricsPlugin
                    .createImage("resume.gif"));
            setToolTipText("&Resume previously paused calculations");
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            try {
                MetricsBuilder.resume();
                pauseAction.setEnabled(MetricsBuilder.canPause());
                resumeAction.setEnabled(MetricsBuilder.canResume());
            } catch (RuntimeException e) {
                Log.logError("PauseAction::run", e);
            }
        }
    }

    /**
     * @return CallersView
     */
    protected LayeredPackageTableView getView() {
        return metricsView;
    }

    /**
     * 
     */
    public void enable() {
        abortAction.setEnabled(MetricsBuilder.canAbort());
        pauseAction.setEnabled(MetricsBuilder.canPause());
        resumeAction.setEnabled(MetricsBuilder.canResume());
        exportAction.setEnabled(true);
        IJavaElement sel = metricsView.getSelection();
        if ((sel != null)
                && ((sel.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) || sel
                        .getElementType() == IJavaElement.JAVA_PROJECT)) {
            graphAction.setEnabled(true);
        } else {
            graphAction.setEnabled(false);
        }
    }

    public void disable() {
        // note that we can NOT disable the abort action here
        abortAction.setEnabled(MetricsBuilder.canAbort());
        pauseAction.setEnabled(MetricsBuilder.canPause());
        resumeAction.setEnabled(MetricsBuilder.canResume());
        exportAction.setEnabled(false);
        graphAction.setEnabled(false);
    }

}