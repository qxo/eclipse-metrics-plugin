package net.sourceforge.metrics.calculators;

/**
 * Calculates MLOC: Method Lines of Code
 * 
 * @author Guillaume Boissier
 * @since 1.3.6
 */
public class MethodLinesOfCode extends AbstractLinesOfCode {

  public MethodLinesOfCode() {
    super("MLOC"); // TODO Const ...
  }

  protected void checkLevelOfComputation(int a_level) throws InvalidSourceException {
    // TODO do we realy need to do some check ?
  }

  /**
   * filter out everything outside of the method body 
   */
  protected String filterSourceToProcess(String a_source) {
    String l_return;
    int l_indexOfMethodBodyStart = a_source.indexOf('{');
    int l_indexOfMethodBodyEnd = a_source.lastIndexOf('}');
		if (l_indexOfMethodBodyStart != -1 && l_indexOfMethodBodyEnd != -1) {
			l_return = a_source.substring(l_indexOfMethodBodyStart+1, l_indexOfMethodBodyEnd).trim();
		} else {
		  l_return = EMPTY_STRING;
		}
		
    return l_return;
  }
  
}