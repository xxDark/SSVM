# Common Debugging tricks

Useful expression to use in the debugger's evaluator to dump the current stack trace:
```java
StreamSupport.stream(ctx.getVM().threadManager.currentOsThread().getBacktrace().spliterator(), false)
        .map(ExecutionContext::getMethod)
        .map(Object::toString)
        .collect(Collectors.joining("\n"))
```