## 🍭 What is a Thread?

Imagine you're in a **toyroom**. Normally, you can only play with **one toy at a time** — you pick up a car, play, then put it down and pick up a doll. That's how a normal program works — **one task at a time**.

Now imagine you have **3 copies of yourself**, and each copy can play with a **different toy at the same time**! 🪀🚂🧸

That's what **threads** are. They are like **mini-workers inside your program**, all running at the same time, each doing their own job.

---

## 🧵 What's Actually Happening in This Code?

This code creates **3 threads** (3 mini-workers), each printing their name every second — all running at the same time.

---

## 🔨 3 Ways to Create a Thread in Java

### 1. 👷 Extending the `Thread` Class

```java
class ThreadExtendedClass extends Thread {
    @Override
    public void run() {
        // What this worker does
    }
}

ThreadExtendedClass thread1 = new ThreadExtendedClass("Class Extended Thread");
thread1.start(); // 👈 Hires and starts the worker
```

**Simple analogy:** You're creating a **new type of worker** by modifying the basic worker blueprint (`Thread`).

> ⚠️ **Downside:** Java only allows **single inheritance**, so if you extend `Thread`, you can't extend any other class.

---

### 2. 📋 Implementing the `Runnable` Interface

```java
class ThreadRunnableClass implements Runnable {
    @Override
    public void run() {
        // What this worker does
    }
}

Thread thread2 = new Thread(new ThreadRunnableClass("Runnable Thread"));
thread2.start();
```

**Simple analogy:** Instead of making a whole new worker type, you're writing a **job description** (`Runnable`) and handing it to a regular worker (`Thread`).

> ✅ **Preferred approach** — keeps your class free to extend others. Separates the **task** from the **thread mechanism**.

---

### 3. ⚡ Lambda Expression (Shortcut!)

```java
Thread thread3 = new Thread(() -> {
    // inline task
});
thread3.start();
```

**Simple analogy:** Instead of writing a full job description document, you just **whisper the instructions** directly to the worker.

> This works because `Runnable` is a **Functional Interface** — it has only ONE method (`run`), so Java lets you replace it with a lambda.

---

## 🛏️ `Thread.sleep(1000)` — Taking a Nap

```java
Thread.sleep(1000); // sleep for 1000 milliseconds = 1 second
```

**Analogy:** The worker finishes printing their name and then takes a **1-second nap** before doing it again.  
Without sleep, the thread would print millions of times per second — just like a toddler hyped up on candy! 🍬

---

## ⛔ `thread1.join()` — Waiting for a Worker to Finish

```java
thread1.join();
```

**Analogy:** The main program (the "boss") says:  
> *"I won't leave the toyroom until thread1 finishes playing."*

In this code, since `thread1` runs `while(true)`, it never finishes — so the main thread will also wait forever. This keeps the program alive.

---

## 🎯 Thread Priority

```java
thread1.getPriority(); // Default is 5
```

| Priority Constant | Value |
|---|---|
| `Thread.MIN_PRIORITY` | 1 |
| `Thread.NORM_PRIORITY` | 5 (default) |
| `Thread.MAX_PRIORITY` | 10 |

> ⚠️ Setting a higher priority is like telling one worker to go first — but the **CPU/OS doesn't always listen**. It's a **hint, not a guarantee**.

---

## 🔁 Thread Lifecycle (State Machine)

```
NEW ──► RUNNABLE ──► RUNNING ──► TERMINATED
                        │
                    WAITING / SLEEPING / BLOCKED
                        │
                    back to RUNNABLE
```

| State | Meaning |
|---|---|
| `NEW` | Thread created but `start()` not called yet |
| `RUNNABLE` | Ready to run, waiting for CPU |
| `RUNNING` | CPU is executing it right now |
| `SLEEPING` | Called `Thread.sleep()` — taking a nap |
| `WAITING` | Called `join()` or `wait()` — waiting for something |
| `TERMINATED` | `run()` method has finished |

---

## 🧠 Underlying Concepts Used

| Concept | What It Means |
|---|---|
| **Multithreading** | Multiple threads running concurrently |
| **Functional Interface** | Interface with exactly 1 abstract method — enables lambdas |
| **Inheritance vs Composition** | `extends Thread` (inheritance) vs `implements Runnable` (composition) |
| **Thread Scheduling** | OS decides which thread gets CPU time |
| **Concurrency** | Multiple tasks making progress in overlapping time periods |

---

## 🎤 Interview Questions & Answers

---

### Q1. What is the difference between `extends Thread` and `implements Runnable`?

| | `extends Thread` | `implements Runnable` |
|---|---|---|
| Inheritance | Blocks extending another class | Free to extend other classes |
| Coupling | Task is tightly coupled to thread | Task is decoupled from thread |
| Reusability | Same object can't be reused across threads | Runnable can be passed to multiple threads |
| **Preferred?** | ❌ | ✅ Yes |

**Answer to remember:** Prefer `Runnable` because it follows the **Separation of Concerns** principle — the task (`Runnable`) is separate from the execution mechanism (`Thread`).

---

### Q2. Why is `Runnable` a functional interface?

Because it has **exactly one abstract method** — `run()`. This makes it eligible to be written as a **lambda expression**, which is why `() -> { ... }` works as a `Runnable`.

---

### Q3. What happens if you call `run()` directly instead of `start()`?

Calling `run()` **does NOT create a new thread**. It runs the method on the **current thread** (sequentially), just like any normal method call. Only `start()` asks the JVM to spawn a new thread.

---

### Q4. What is `Thread.sleep()` and does it release the lock?

`Thread.sleep(ms)` pauses the current thread for the given milliseconds.  
> ⚠️ It **does NOT release** any lock the thread holds (unlike `Object.wait()`).

---

### Q5. What is `join()` used for?

`thread.join()` makes the **calling thread wait** until the thread it's called on finishes execution.  
Used when one task depends on another completing first.

---

### Q6. What is the difference between `sleep()` and `wait()`?

| | `Thread.sleep()` | `Object.wait()` |
|---|---|---|
| Defined in | `Thread` class | `Object` class |
| Releases lock? | ❌ No | ✅ Yes |
| Requires `synchronized`? | ❌ No | ✅ Yes |
| Woken up by | Timer expiry | `notify()` / `notifyAll()` |

---

### Q7. Are threads in this code daemon or user threads?

By default, threads are **user threads** (non-daemon). The JVM keeps running until all user threads finish. To make a thread a daemon (background) thread:
```java
thread1.setDaemon(true); // must be called before start()
```
Daemon threads are killed automatically when all user threads finish.

---

### Q8. What is a race condition and does this code have one?

A **race condition** happens when multiple threads access shared data and the result depends on the order of execution. In this code, threads only **print** independently — no shared mutable data — so **no race condition** here. But in real apps, always protect shared state with `synchronized`, `Lock`, or atomic classes.

---

### Q9. What does `while(true)` mean in threads?

It means the thread runs **indefinitely** (infinite loop). To stop it cleanly, use a **volatile flag**:
```java
private volatile boolean running = true;

while (running) { ... }

public void stop() { running = false; }
```
Using `volatile` ensures the flag's updated value is **visible to all threads** immediately.

---

### Q10. What is thread priority and is it guaranteed?

Thread priority is a **hint to the scheduler** — higher priority threads are *more likely* to get CPU time first, but it is **platform-dependent and not guaranteed**. Never write logic that depends on thread priority for correctness.

---

### Read about [Java Executioner Service](https://github.com/vaibhav25-mnnit/java-multithreading/blob/master/ThreadPools/Readme.md) (V.V.V Imp for interviews)

---


Made with ❤️ by [@vaibhav25-mnnit](https://github.com/vaibhav25-mnnit)