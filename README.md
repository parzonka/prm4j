# prm4j

prm4j is a light-weight runtime verification library. It can be used to efficiently monitor and detect patterns of object interactions by matching the program’s execution trace against a parametric property. This property describes a pattern containing free variables typed by classes. During runtime this pattern is matched by binding the variables to concrete object instances.

A Java target application can be intrumented using AspectJ so that relevant events may be passed to prm4j. Latter implements the specification and matching of parametric properties. prm4j also provides an API to create and pass events to the monitor.  Arbitrary code may be executed when a match is detected. This may be a simple warning or recovery code performing changes in the instrumented target application.

For more information refer to the [master's thesis][2] of Mateusz Parzonka ([Technische Universität Darmstadt][3] supervised by [Eric Bodden Ph.D.][4]).

## Example

A pattern, commonly known in the runtime verification community, is the *UnsafeIterator* property (also known as *FailSafeIterator*). The UnsafeIterator is satisfied when a Collection in the process of iteration is modified and iteration continues. Many programmers have experienced Collection implementations in the JDK throwing a [ConcurrentModificationException][1] in this case. The example shows an AspectJ aspect which defines pointcuts selecting Iterator creations and updates as well as Collection mutations. The UnsafeIterator property is specified by a pattern defined by a finite state machine (FSM). This pattern is tried to be matched against the event trace created by interactions of all Collections and their Iterators. In the given aspect, a simple sysout is printed *before* the exception is thrown. Technically it is possible to execute arbitrary code in case of a match.

```aspectj
public aspect UnsafeIterator {
    
    private final ParametricMonitor pm;
    private final Alphabet alphabet = new Alphabet();

    // parameters
    private final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
    private final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

    // symbols
    private final Symbol2<Collection, Iterator> createIter = alphabet.createSymbol2("createIter", c, i);
    private final Symbol1<Collection> updateColl = alphabet.createSymbol1("updateColl", c);
    private final Symbol1<Iterator> useIter = alphabet.createSymbol1("useIter", i);

    // match handler
    public final  MatchHandler matchHandler = MatchHandler.SYS_OUT;
    
    final FSM fsm = new FSM(alphabet);

    public UnsafeIterator() {
	
		// fsm states
		final FSMState initial = fsm.createInitialState();
		final FSMState s1 = fsm.createState();
		final FSMState s2 = fsm.createState();
		final FSMState error = fsm.createAcceptingState(matchHandler);
    	
		// fsm transitions
		initial.addTransition(updateColl, initial);
		initial.addTransition(createIter, s1);
		s1.addTransition(useIter, s1);
		s1.addTransition(updateColl, s2);
		s2.addTransition(updateColl, s2);
		s2.addTransition(useIter, error);
    	
		// parametric monitor creation
		pm = ParametricMonitorFactory.createParametricMonitor(new FSMSpec(fsm));
	
    }

	// pointcut matches when an Iterator is created
    after(Collection c) returning (Iterator i) : (call(Iterator Collection+.iterator()) && target(c)) {
		pm.processEvent(createIter.createEvent(c, i));
    }

	// pointcut matches when a Collection is updated
    after(Collection c) : ((call(* Collection+.remove*(..)) || call(* Collection+.add*(..))) && target(c)) {
		pm.processEvent(updateColl.createEvent(c));
    }

	// pointcut matches when an Iterator is used
    before(Iterator i) : (call(* Iterator.next()) && target(i)) {
		pm.processEvent(useIter.createEvent(i));
    }

}
```

## Usage

Maven dependency:

```xml
<dependency>
    <groupId>com.github.parzonka</groupId>
    <artifactId>prm4j</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

Repository:

```xml
<repository>
    <id>parzonka.github.com-snapshots</id>
    <url>http://parzonka.github.com/m2/snapshots</url>
</repository>
```

## Developer

prm4j was developed as part of the [master's thesis][2] of Mateusz Parzonka at [Technische Universität Darmstadt][3] supervised by [Eric Bodden Ph.D.][4]

  [1]: http://docs.oracle.com/javase/6/docs/api/java/util/ConcurrentModificationException.html
  [2]: https://github.com/parzonka/prm4j/raw/master/docs/msc-thesis.pdf
  [3]: http://www.ec-spride.tu-darmstadt.de/csf/sse/index.en.jsp
  [4]: http://www.bodden.de/
