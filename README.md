Command-line tool to kill a single thread in a Java VM, using the Oracle JDK 6+ Attach API.

Download [`jkillthread.jar`](https://github.com/binarynoise/jkillthread/releases/latest)
and run using the `java` command from a JRE (11+, use the targets JRE is possible) to get usage instructions.
Essentially you pass a process ID (or name substring) and then a thread name (or substring):

```sh
java -jar jkillthread.jar 12345 "rogue HTTP handler"
```

(`jps -lm` is useful for finding a process ID. `jstack 12345` can be used to see currently running threads.)

Beware that killing a thread in Java (`Thread.stop`) can have various effects, depending on what it was doing:

* It might die quietly and that is that.
* It might die, but print or log a stack trace somewhere first.
* It might die but a similar thread be automatically relaunched by some sentinel.
* It might not die because it is blocked in some native call which does not honor `stop`.
* It might go into an odd state and not release locks that it should have. (Theoretically. I have never actually seen this happen.)

But as `Thread.stop()` is now unsupported, this now uses `Thread.interrupt()` instead which should cause fewer issues.

Caveat interfector!
