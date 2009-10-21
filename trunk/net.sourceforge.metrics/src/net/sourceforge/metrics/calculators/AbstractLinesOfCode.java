package net.sourceforge.metrics.calculators;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Replace old LOC calculator which does not handle comments correctly.
 * This implementatin uses the Eclipse IScanner to parse the code
 * 
 * @author Guillaume Boissier
 * @since 1.3.6
 */
public abstract class AbstractLinesOfCode extends Calculator {

  protected final static String EOL = System.getProperty("line.separator");
  protected final static String EMPTY_STRING = "";
  
  public AbstractLinesOfCode(String a_name) {
    super(a_name);
  }

  public void calculate(AbstractMetricSource a_source) throws InvalidSourceException {
    checkLevelOfComputation(a_source.getLevel());
    if(a_source.getASTNode() != null) {
      try{
        String l_sourceTxt = getSource(a_source);
        a_source.setValue(new Metric(name, calculateNumberOfLines(l_sourceTxt)));
      } catch(JavaModelException l_jme){
        Log.logError("Error in AbstractLinesOfCode", l_jme);
      }
    } else {
      Log.logMessage("Error in AbstractLinesOfCode: no AstNode associated with Level(" + a_source.getLevel() + ")");
    }
  }

  protected abstract void checkLevelOfComputation(int a_level) throws InvalidSourceException;

  public String getSource(AbstractMetricSource a_metricSource) throws JavaModelException {
    ASTNode l_astNode = a_metricSource.getASTNode();
    int start = l_astNode.getStartPosition();
    int length = l_astNode.getLength();

    ICompilationUnit l_unit = a_metricSource.getCompilationUnit();
    String source = l_unit.getSource();
    return source.substring(start, start + length);
  }

  /**
   * parse the given source using the Eclipse IScanner to count the number of lines
   * @param a_source
   * @return number of lines in a_source, not counting comments
   * 
   * @throws InvalidInputException
   */
  public int calculateNumberOfLines(String a_source) {
    String l_srcToCount = filterSourceToProcess(a_source).trim();
    Set l_lineSet = new HashSet();
    IScanner l_scanner = ToolFactory.createScanner(false,false,true,true);
    l_scanner.setSource(l_srcToCount.toCharArray());
    try {
		while (true) {
		  int token = l_scanner.getNextToken();
		      if (token == ITerminalSymbols.TokenNameEOF) break;
		      int l_startpos = l_scanner.getCurrentTokenStartPosition();
		      int l_lineNb = l_scanner.getLineNumber(l_startpos);
		      l_lineSet.add(new Integer(l_lineNb));
		}
	} catch (InvalidInputException e) {
		Log.logError("Invalid source in AbstractLinesOfCode", e);
	}
    return l_lineSet.size();
  }
  
  /**
   * subclasses have to determine what parts of the source to use
   * (This base class is used to count lines in methods as well as entire types)
   * 
   * @param a_source
   * @return
   */
  protected abstract String filterSourceToProcess(String a_source);

 
  
}
