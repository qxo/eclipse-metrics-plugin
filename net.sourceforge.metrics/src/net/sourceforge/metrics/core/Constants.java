/*
 * Copyright (c) 2003 Frank Sauer. All rights reserved.
 *
 * Licenced under CPL 1.0 (Common Public License Version 1.0).
 * The licence is available at http://www.eclipse.org/legal/cpl-v10.html.
 *
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 *
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES  NO REPRESENTATIONS OR WARRANTIES,
 * EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL THE
 * AUTHOR  BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT LIMITATION, LOST REVENUE,  PROFITS
 * OR DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL  OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF  LIABILITY, ARISING OUT OF OR RELATED TO
 * ANY FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR
 * HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *
 * $id$
 */
package net.sourceforge.metrics.core;

/**
 * static constants used in many places
 * 
 * @author Frank Sauer
 */
public interface Constants {

	/** project level source */
	public final static int PROJECT = 6;
	/** source folder level source */
	public final static int PACKAGEROOT = 5;
	/** package level source */
	public final static int PACKAGEFRAGMENT = 4;
	/** compilation unit level source */
	public final static int COMPILATIONUNIT = 3;
	/** class level source */
	public final static int TYPE = 2;
	/** method level source */
	public final static int METHOD = 1;

	// basic metric ids

	/** "NMI" */
	public final static String NUM_INHERITED_METHODS = "NMI";
	
	/** "NBD" */
	public final static String NESTEDBLOCKDEPTH = "NBD";
	/** "PAR" */
	public final static String PARMS = "PAR";
	/** "VG" */
	public final static String MCCABE = "VG";
	/** "NOM" */
	public final static String NUM_METHODS = "NOM";
	/** "NONM" - 'Normal Methods - excludes getters and setters'**/
	public final static String NUM_NORMAL_METHODS = "NONM";
	/** "NSM" */
	public final static String NUM_STAT_METHODS = "NSM";
	/** "NSF" */
	public final static String NUM_STAT_FIELDS = "NSF";
	/** "NOF" */
	public final static String NUM_FIELDS = "NOF";
	/** "NOC" */
	public final static String NUM_TYPES = "NOC";
	/** "NOP" */
	public final static String NUM_PACKAGES = "NOP";
	/** "NOI" */
	public final static String NUM_INTERFACES = "NOI";
	/** "DIT" */
	public final static String INHERITANCE_DEPTH = "DIT";
	/** "NSC" */
	public final static String SUBCLASSES = "NSC";
	/** "NUC" */
	public final static String SUPERCLASSES = "NUC";
	/** "U" */
	public final static String REUSE_RATIO = "U";
	/** "SIX" */
	public final static String SPECIALIZATION_IN = "SIX";
	/** "SIX2" */
	public final static String SPECIALIZATION_IN2 = "SIX2";
	/** "NORM" */
	public final static String NORM = "NORM";
	/** "WMC" */
	public final static String WMC = "WMC";
	/** "LCOM" */
	public final static String LCOM = "LCOM";
	/** "RMC" */
	public final static String RMC = "RMC";
	/** "CA" */
	public final static String CA = "CA";
	/** "CE" */
	public final static String CE = "CE";
	/** "RMI" */
	public final static String RMI = "RMI";
	/** "RMA" */
	public final static String RMA = "RMA";
	/** "RMD" */
	public final static String RMD = "RMD";
	/** "MLOC" */
	public final static String MLOC = "MLOC";
	/** "TLOC" */
	public final static String TLOC = "TLOC";
	
	
	/**QMOOD Metrics*/
	/** "CIS" - Class Interface Size */
	public final static String CIS = "CIS";
	/** "MOA" - Measure of Aggregation - Implemented in NumberOfAttributes Calc */
	public final static String MOA = "MOA";
	/** "DAM"  - Data Access Metric */
	public final static String DAM = "DAM";
	/** "ANA"  - Average Number of Ancestors */
	public final static String ANA = "ANA";
	/** "NOPM" - Number of Polymorhic Methods - Originally NOP on the QMOOD Paper  */
	public final static String NOPM = "NOPM";
	/** "CAM" - Cohesion Among Methods of Class */
	public final static String CAM = "CAM";
	/** "MFA" - Measure of Functional Abstraction */
	public final static String MFA = "MFA";
	/** "DCC" - Direct Class Coupling */
	public final static String DCC = "DCC";
	/** "NOH" - Number of Hierarchies */
	public final static String NOH = "NOH";
	/** "DSC" - Design Size in Classes - Equivalent to the NUM_TYPES metric. */
	public final static String DSC = "DSC";
	
	public final static String[] QMOODMetrics = {CIS, MOA, DAM, ANA, NOPM, CAM, MFA, DCC, NOH, DSC};
	
	/** QMOOD Evaluation Functions */
	/** Flexibility */
	public final static String FLE = "FLE";
	/** Reusability */
	public final static String REU = "REU";
	/** Understandability */
	public final static String UND = "ENT";
	/** Effectiveness */
	public final static String EFE = "EFE";
	/** Functionality */
	public final static String FUN = "FUN";
	/** Extendibility */
	public final static String EXT = "EXT";
	
	public final static String[] QMOODEvaluationFunctions = {FLE, REU, UND, EFE, FUN, EXT};

	// scopes for averages and maxima
	/** "method" */
	public final static String PER_METHOD = "method";
	/** "type" */
	public final static String PER_CLASS = "type";
	/** "packageFragment" */
	public final static String PER_PACKAGE = "packageFragment";

	public static final String[] PER_ARRAY = new String[] { PER_PACKAGE, PER_CLASS, PER_METHOD };

	// namespaces for persistent storage and in-memory caching
	public final static String PLUGIN_ID = "net.sourceforge.metrics";

	public final static int FRACTION_DIGITS = 3;

}
