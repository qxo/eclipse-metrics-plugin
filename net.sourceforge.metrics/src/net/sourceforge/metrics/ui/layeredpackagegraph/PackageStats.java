package net.sourceforge.metrics.ui.layeredpackagegraph;

/**
 * Holds layer statistics for a package
 */
public class PackageStats implements Comparable<PackageStats> {
	private final String packageName;
	private int layer;
	private boolean tangle;

	public PackageStats(String packageName) {
		this.packageName = packageName;
		layer = 0;
		tangle = false;
	}

	public int getLayer() {
		return this.layer;
	}

	/**
	 * Raises the layer of this object to the given level, if it is not already as high or higher.
	 */
	public void raiseTo(int level) {
		layer = Math.max(layer, level);
	}

	public void setTangle() {
		this.tangle = true;
	}

	public boolean isTangle() {
		return tangle;
	}

	public String getPackageName() {
		return packageName;
	}

	public int compareTo(PackageStats other) {
		int result = layer - other.layer;
		if (result != 0) {
			return result;
		}

		return packageName.compareTo(other.packageName);
	}

}