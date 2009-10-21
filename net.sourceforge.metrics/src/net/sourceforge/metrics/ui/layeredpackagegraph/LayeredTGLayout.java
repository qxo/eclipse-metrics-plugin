package net.sourceforge.metrics.ui.layeredpackagegraph;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGLayout;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.graphelements.TGForEachEdge;
import com.touchgraph.graphlayout.graphelements.TGForEachNode;
import com.touchgraph.graphlayout.graphelements.TGForEachNodePair;

/**
 * Implements TGLayout and is the thread responsible for graph layout. It
 * updates the real coordinates of the nodes in the graphEltSet object. TGPanel
 * sends it resetDamper commands whenever the layout needs to be adjusted. After
 * every adjustment cycle, we trigger a repaint of the TGPanel. In moving the
 * nodes, we try to mimick physical objects with mass that are moved by forces
 * depending on the distance between the nodes. All this happens in an
 * environment that dampens movement, like a container of stiffening glue.
 */
public class LayeredTGLayout implements TGLayout {
    private TGPanel tgPanel;
    private Thread relaxer;
    //  A low damper value causes the graph to move slowly
    private double damper = 0.0;
    //  Keep an eye on the fastest moving node to see if the graph is stabilizing
    private double maxMotion = 0;
    private double lastMaxMotion = 0;
    // It's sort of a ratio, equal to lastMaxMotion/maxMotion-1
    private double motionRatio = 0;
    // When damping is true, the damper value decreases
    private boolean damping = true;

    // Rigidity has the same effect as the damper, except that it's a constant a
    // low EDGE_FORCE value causes things to go slowly. A value that's too high
    // will cause oscillation
    private Node dragNode = null;
    protected static final double DESIRED_SEPARATION = 50;

    public LayeredTGLayout(TGPanel tgp) {
        tgPanel = tgp;
        //graphEltSet = tgPanel.getGES();
        relaxer = null;
    }

    public void setDragNode(Node n) {
        dragNode = n;
    }


    //relaxEdges is more like tense edges up. All edges pull nodes closer
    // together;
    private synchronized void tenseEdges() {
        TGForEachEdge fee = new TGForEachEdge() {
            public void forEachEdge(Edge e) {
                double vx = e.to.x - e.from.x;
                double len = Math.abs(vx);
                double dx = vx * len * getEdgeForce();
                //dx /= (e.getLength() * 100);
                e.from.dx += dx / e.from.visibleEdgeCount();
                e.to.dx -= dx / e.to.visibleEdgeCount();
            }

            private double getEdgeForce() {
                return damper / 4000.0;
            }
        };

        tgPanel.getGES().forAllEdges(fee);
    }

    private synchronized void strongNodeForce() {
        TGForEachNodePair fenp = new TGForEachNodePair() {
            public void forEachNodePair(Node n1, Node n2) {
                if (LayeredPackageTableView.getLayer(n1.getID()) == LayeredPackageTableView
                        .getLayer(n2.getID())) {
                    double dx;
                    double vx = n1.x - n2.x;
                    double len = Math.abs(vx);
                    double labelWidth = ((n1.getID().length() + n2.getID()
                            .length()) * 4);
                    if (len == 0) {
                        dx = Math.random();
                    } else if (len < getDesiredSeparation(labelWidth)) {
                        dx = vx > 0 ? (getDesiredSeparation(labelWidth) - len)
                                : -(getDesiredSeparation(labelWidth) - len);
                    } else {
                        dx = 0;
                    }

                    if (!n1.justMadeLocal && !n1.markedForRemoval
                            && (n2.justMadeLocal || n2.markedForRemoval)) {
                        double massfade = (n2.markedForRemoval ? n2.massfade
                                : 1 - n2.massfade);
                        massfade *= massfade;
                        dx *= massfade;
                    }
                    if ((!false || !n2.justMadeLocal) && !n2.markedForRemoval
                            && (n1.justMadeLocal || n1.markedForRemoval)) {
                        double massfade2 = (n1.markedForRemoval ? n1.massfade
                                : 1 - n1.massfade);
                        massfade2 *= massfade2;
                        dx *= massfade2;
                    }

                    dx *= 1.1 * (1 - damper);
                    n1.dx += dx;
                    n2.dx -= dx;
                }
            }

            private double getDesiredSeparation(double labelWidth) {
                return (DESIRED_SEPARATION + labelWidth); //* (1 - damper);
            }
        };

        tgPanel.getGES().forAllNodePairs(fenp);
    }

    public void startDamper() {
        damping = true;
    }

    public void stopDamper() {
        damping = false;
        damper = 1.0; //A value of 1.0 means no damping
    }

    public synchronized void resetDamper() { //reset the damper, but don't keep
        // damping.
        damping = true;
        damper = 1.0;
    }

    public void stopMotion() { // stabilize the graph, but do so gently by
        // setting the damper to a low value
        damping = true;
        if (damper > 0.3) {
            damper = 0.3;
        } else {
            damper = 0;
        }
    }

    public void damp() {
        if (damping) {
            if (maxMotion < 1 && damper > 0.1) {
                damper -= 0.1;
            } else if (maxMotion < 10 && damper > 0.01) {
                damper -= 0.01;
            } else if (damper > 0.001) {
                damper -= 0.001;
            }
            //}
            if (damper > 0 && damper < 0.003) {
                damper = 0;
            }
        }
    }


    private synchronized void moveNodes() {
        lastMaxMotion = maxMotion;
        final double[] maxMotionA = new double[1];
        maxMotionA[0] = 0;

        TGForEachNode fen = new TGForEachNode() {
            public void forEachNode(Node n) {
                if (n != tgPanel.getSelect()) {
                    double dx = (n.dx + n.dy) / 2;
                    n.dy = n.dx;
                    // resistance
                    dx /= 2;
                    // don't move faster than 30 units at a time.
                    dx = Math.max(-30, Math.min(30, dx));
                    double distMoved = Math.abs(dx);

                    if (!n.fixed && !(n == dragNode)) {
                        n.x += dx;
                        // Slow down, but don't stop. Nodes in motion store
                        // momentum. This helps when the force on a node is very
                        // low, but you still want to get optimal layout.
                        n.dx = dx / 2;
                        n.dx = 0;
                    }
                    maxMotionA[0] = Math.max(distMoved, maxMotionA[0]);

                    if (!n.justMadeLocal && !n.markedForRemoval) {
                        n.massfade = 1;
                    } else {
                        if (n.massfade >= 0.004) n.massfade -= 0.004;
                    }
                }

            }
        };

        tgPanel.getGES().forAllNodes(fen);

        maxMotion = maxMotionA[0];
        if (maxMotion > 0) motionRatio = lastMaxMotion / maxMotion - 1;
        else motionRatio = 0;

        damp();

    }

    private synchronized void relax() {
        for (int i = 0; i < 1; i++) {
            centerForce();
            tenseEdges();
            weakNodeForce();
            strongNodeForce();
            moveNodes();
        }
        tgPanel.repaintAfterMove();
    }

    private void centerForce() {
        if (tgPanel.getSelect() == null) {
            return;
        }

        final double[] sum = new double[1];
        final double[] count = new double[1];
        TGForEachNode sumFen = new TGForEachNode() {
            public void forEachNode(Node n) {
                if (n != tgPanel.getSelect()) {
                    sum[0] += n.x - tgPanel.getSelect().x;
                    count[0]++;
                }
            }
        };

        tgPanel.getGES().forAllNodes(sumFen);
        final double mean = sum[0] / count[0];

        TGForEachNode fen = new TGForEachNode() {
            public void forEachNode(Node n) {
                if (n != tgPanel.getSelect()) {
                    double dx = -mean * damper;
                    n.dx += dx;
                }
            }
        };

        tgPanel.getGES().forAllNodes(fen);
    }

    /**
     * Adds movement that repels the nodes depending on their proximity. It is a
     * long reaching, but weak force. It is an early force that diminishes
     * towards the end of the layout cycle.
     */
    private void weakNodeForce() {
        TGForEachNodePair fenp = new TGForEachNodePair() {
            public void forEachNodePair(Node n1, Node n2) {
                if (LayeredPackageTableView.getLayer(n1.getID()) == LayeredPackageTableView
                        .getLayer(n2.getID())) {
                    double dx;
                    double vx = n1.x - n2.x;
                    double len = Math.abs(vx);
                    if (len == 0) {
                        dx = Math.random();
                    } else {
                        dx = 0.01 / (vx * len);
                    }

                    if (!n1.justMadeLocal && !n1.markedForRemoval
                            && (n2.justMadeLocal || n2.markedForRemoval)) {
                        double massfade = (n2.markedForRemoval ? n2.massfade
                                : 1 - n2.massfade);
                        massfade *= massfade;
                        dx *= massfade;
                    }
                    if ((!false || !n2.justMadeLocal) && !n2.markedForRemoval
                            && (n1.justMadeLocal || n1.markedForRemoval)) {
                        double massfade2 = (n1.markedForRemoval ? n1.massfade
                                : 1 - n1.massfade);
                        massfade2 *= massfade2;
                        dx *= massfade2;
                    }

                    dx *= damper;
                    n1.dx += dx;
                    n2.dx -= dx;
                }
            }

            private double getDesiredSeparation(double labelWidth) {
                return (DESIRED_SEPARATION + labelWidth); //* (1 - damper);
            }
        };

        tgPanel.getGES().forAllNodePairs(fenp);
    }

    private void myWait() { //I think it was Netscape that caused me not to use
        // Wait, or was it java 1.1?
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //break;
        }
    }

    public void run() {
        Thread me = Thread.currentThread();
        //      me.setPriority(1); //Makes standard executable look better, but the
        // applet look worse.
        while (relaxer == me) {
            relax();
            try {
                Thread.sleep(20); //Delay to wait for the prior repaint
                // command to finish.
                while (damper < 0.1 && damping && maxMotion < 0.1)
                    myWait();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void start() {
        relaxer = new Thread(this, "Layout");
        relaxer.start();
    }

    public void stop() {
        relaxer = null;
    }

} // end com.touchgraph.graphlayout.TGStarLayout
