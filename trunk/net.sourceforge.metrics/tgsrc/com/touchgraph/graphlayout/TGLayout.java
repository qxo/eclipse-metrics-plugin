package com.touchgraph.graphlayout;

public interface TGLayout extends Runnable {
	public abstract void startDamper();

	public abstract void stopDamper();

	public abstract void resetDamper();

	public abstract void stopMotion();

	public abstract void damp();

	public abstract void run();

	public abstract void start();

	public abstract void stop();

	public abstract void setDragNode(Node node);
}