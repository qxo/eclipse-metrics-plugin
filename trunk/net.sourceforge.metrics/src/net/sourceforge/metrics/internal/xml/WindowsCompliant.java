package net.sourceforge.metrics.internal.xml;

public class WindowsCompliant extends MetricsFirstExporter {
	
	protected String formatXMLStr(String handle) {
		return handle.replaceAll("<", "[@").replaceAll(">", "@]").replaceAll("\\\\", "_");
	}
	
}
