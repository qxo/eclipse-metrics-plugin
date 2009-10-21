package net.sourceforge.metrics.calculators;

import net.sourceforge.metrics.core.Constants;

/**
 * Calculates TLOC: Total Lines of Code
 * 
 * @author Guillaume Boissier
 * @since 1.3.6
 */
public class TotalLinesOfCode extends AbstractLinesOfCode {

	public TotalLinesOfCode() {
		super(Constants.TLOC);
	}

	@Override
	protected void checkLevelOfComputation(int a_level) throws InvalidSourceException {
		// TODO do we realy need to do some check ?
	}

	/**
	 * Use everything
	 */
	@Override
	protected String filterSourceToProcess(String a_source) {
		return a_source;
	}

}
