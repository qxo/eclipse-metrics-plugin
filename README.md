Metrics 3 - Eclipse Metrics Plugin Continued 'Again' 
=======
The origin code from svn://svn.code.sf.net/p/metrics2/code/trunk
And The Master code from: https://github.com/leonardobsjr/metrics3

This is a continuation of the Eclipse Metrics Plugin 2, which was first created by [Frank Sauer](http://sourceforge.net/users/sauerf) and then continued by Keith Cassell and [Guillaume Boissier](http://sourceforge.net/users/gboissier).

Check http://metrics2.sourceforge.net/ to see 

#Compile Guide
*. import projects (net.sourceforge.metrics,net.sourceforge.metrics.feature) to eclipse workspace
*. change some code if you need
*. export updatesite archive zip: select project==> export ==> Plugin-in Development==> Deployable feature => select feature "net.sourceforge.metrics"  and Archive file loation  ==> Finish 



#Metrics Included

* Number of Classes (NOC)
	*  Total number of classes in the selected scope.
* Number of Children
	* Total number of direct subclasses of a class. A class implementing an interface counts as a direct child of that interface.
* Number of Interfaces (NOI)
	* Total number of interfaces in the selected scope.
* Depth of Inheritance Tree (DIT)
	* Distance from class Object in the inheritance hierarchy. 
* Number of Overidden Methods (NORM)
	* Total number of methods in the selected scope that are overridden from an ancestor class.
* Number of Methods Inherited (NMI)
	* Total number of methods in the selected scope that are inherited from all ancestor classes.
* Number of Methods (NOM)
	* Total number of methods defined in the selected scope.
* Number of Fields (NOF)
 	* Total number of fields defined in the selected scope.
* Lines of Code
  * Total Lines of Code (TLOC)
	  * Total lines of code that will counts non-blank and non-comment lines in a compilation unit. usefull for thoses interested in computed KLOC.
  * Methods Lines of Code (MLOC)
	  * Method lines of code will counts and sum non-blank and non-comment lines inside method bodies.
* Specialization Index (SIX)
	*  Average of the specialization index, defined as NORM * DIT / NOM. This is a class level metric.
* Specialization Index 2 (SIX2)
	*  Average of the specialization index, defined as NORM * DIT / (NOM + NMI) as defined in "M. Lorenz, J. Kidd, Object-Oriented Software Metrics, Prentice Hall, 1994". This is a class level metric.
* McCabe Cyclomatic Complextity
	*  Counts the number of flows through a piece of code. Each time a branch occurs (if, for, while, do, case, catch and the ?: ternary operator, as well as the && and || conditional logic operators in expressions) this metric is incremented by one. Calculated for methods only. For a full treatment of this metric see [McCabe](http://www.mccabe.com/nist/nist_pub.php).
* Weighted Methods per Class (WMC)
	* Sum of the McCabe Cyclomatic Complexity for all methods in a class.
* Lack of Cohesion of Methods (LCOM*)
	* A measure for the Cohesiveness of a class. Calculated with the Henderson-Sellers method (LCOM*, see page 147). If (m(A) is the number of methods accessing an attribute A, calculate the average of m(A) for all attributes, subtract the number of methods m and divide the result by (1-m). A low value indicates a cohesive class and a value close to 1 indicates a lack of cohesion and suggests the class might better be split into a number of (sub)classes.

####Robert C. Martin Suite

The following are the coupling metrics as defined by Robert Martin in ["OO Design Quality Metrics, An Analysis of Dependencies"](http://www.objectmentor.com/resources/articles/oodmetrc.pdf), and more recently in his book named "Agile Software Development, Principles, Patterns and Practices".

* Afferent Coupling (Ca)
	*  The number of classes outside a package that depend on classes inside the package. 
* Efferent Coupling (Ce)
	*  The number of classes inside a package that depend on classes outside the package. 
* Instability (I)
	* Calculated as  Ce / (Ca + Ce) .
* Abstractness (A)
	*  The number of abstract classes (and interfaces) divided by the total number of types in a package.
* Normalized Distance from Main Sequence (RMD)
	*  | A + I - 1 |, this number should be small, close to zero for good packaging design.

####Quality Model for Object-Oriented Design (QMOOD)
The QMOOD model was design by Jagdish Bansiya and Carl G. Davis as a new way to access the software quality by making usage of certain *design properties*, represented by a single metric each, that can be used to calculate certain *quality atributes*. The paper [A hierarchical model for object-oriented design quality assessment](http://dx.doi.org/10.1109/32.979986) paper details the model. 

The original metrics were implemented in C++, so some implementation details needed to be adjusted for the Java language. Those details were taken from the Mark O'Keeffe &Mel Ó Cinnéide [Search-based refactoring for software maintenance](http://dx.doi.org/10.1016/j.jss.2007.06.003).

#####Design Properties
* Design Size | Design Size in Class (DSC)
	* Total number of source classes.
* Hierarchies | Number of Hierarchies (NOH)
	* A count of the number of class hierarchies in the design.
* Abstraction | Average Number of Ancestors (ANA)
	* The average number of classes from which each class inherits information.
* Encapsulation | Data Access Metrics (DAM)
	* The ratio of the number of private (protected) attributes to the total number of attributes declared in the class. Interpreted as the average across all design classes with at least one attribute, of the ratio of non-public to total attributes in a class.
* Coupling | Direct Class Coupling (DCC)
	* A count of the different number of classes that a class is directly related to. The metric includes classes that are directly related by attribute declarations and message passing (parameters) in methods. Interpreted as an average over all classes when applied to a design as a whole; a count of the number of distinct user-defined classes a class is coupled to by method parameter or attribute type. The java.util.Collection classes are counted as user-defined classes if they represent a collection of a user-defined class.
* Cohesion | Cohesion Among Methods in Class (CAM)
	* Represents the relatedness among methods of a class, computed using the summation of the intersection of parameters of a method with the maximum independent set of all parameter types in the class. Constructors and static methods are excluded.
* Composition | Measure of Aggregation (MOA)
	* A count of the number of data declarations whose types are user-defined classes. Interpreted as the average value across all design classes. We define ‘user defined classes’ as non-primitive types that are not included in the Java standard libraries and collections of user-defined classes from the java.util.collections package.
* Inheritance | Measure of Functional Abstraction (MFA)
	* A ratio of the number of methods inherited by a class to the number of methods accessible by member methods of the class. Interpreted as the average across all classes in a design of the ratio of the number of methods inherited by a class to the total number of methods available to that class, i.e. inherited and defined methods.
* Polymorphism | Number of Polymorphic Methods (NOP)
	* The count of the number of the methods that can exhibit polymorphic behaviour. Interpreted as the average across all classes, where a method can exhibit polymorphic behaviour if it is overridden by one or more descendent classes.
* Messaging | Class Interface Size (CIS)
	* A count of the number of public methods in a class. Interpreted as the average across all classes in a design.
* Complexity | Number of Methods (NOM)
	*  A count of all the methods defined in a class. Interpreted as the average across all classes in a design.
	
#####Quality Attributes

* Reusability
* Flexibility
* Understandability
* Effectiveness
* Extendibility
* Functionality
