package net.sourceforge.metrics.internal.xml;

public class WinCompliantExporter extends MetricsFirstExporter {

	@Override
	protected String formatXMLStr(String handle) {
		return handle.replaceAll("<", "[@").replaceAll(">", "@]").replaceAll("\\\\", "_");
	}

}
