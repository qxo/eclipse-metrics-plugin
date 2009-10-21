package net.sourceforge.metrics.calculators;


/**
 * Calculates TLOC: Total Lines of Code
 * @author Guillaume Boissier
 * @since 1.3.6
 */
public class TotalLinesOfCode extends AbstractLinesOfCode {

	public TotalLinesOfCode() {
	  super("TLOC"); // TODO const ...
	}

  protected void checkLevelOfComputation(int a_level) throws InvalidSourceException {
    // TODO do we realy need to do some check ?
  }

  /**
   * Use everything
   */
  protected String filterSourceToProcess(String a_source) {
    return a_source;
  }

  
}