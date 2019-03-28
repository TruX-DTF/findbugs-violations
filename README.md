# Mining Fix Patterns for FindBugs Violations 

Collection unfixed and fixed violations with project "violation-collection".
```
violation-collection/src/main/scala/edu./lu/uni/serval/alarm/tracking/UnfixedAlarmCollector.scala
violation-collection/src/main/scala/edu./lu/uni/serval/alarm/tracking/UnfixedAlarmCollectorByVType.scala
violation-collection/src/main/scala/edu./lu/uni/serval/alarm/tracking/FixedAlarmCollector.scala
```

Parsing the information of fixed and unfixed violations with GitTraverse and Parser.
```
Parser/src/main/java/edu/lu/uni/serval/main/Main.java
```

Mining patterns for violations and fixing violations with PatternMining.
```
PatternMining/src/main/java/edu/lu/uni/serval/mining/fix/pattern/PatternMiner.java
PatternMining/src/main/java/edu/lu/uni/serval/mining/pattern/fixedViolation/PatternMiner.java
PatternMining/src/main/java/edu/lu/uni/serval/mining/pattern/unfixedViolation/PatternMiner.java
PatternMining/src/main/java/edu/lu/uni/serval/mining/pattern/violation/PatternMiner.java
```

"Mined Fix Patterns" discloses all extracted fix patterns.

"GumTree" is not the same as the original GumTree since we made some changes to be suitable for our requirements.

Compilation
----------

```
# install simple-utils
cd simple-utils
mvn install

# cd ../PatternMining
mvn install
```
