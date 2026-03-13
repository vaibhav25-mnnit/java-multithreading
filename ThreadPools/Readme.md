# 🏢 Java ExecutorService & Future 

## 🗺️ The Full Picture — Java's Concurrency World
```
Thread Pool      →  is a CONCEPT
ExecutorService  →  is Java's way to IMPLEMENT that concept
```
Just like:
```
Sorting         →  is a CONCEPT
Arrays.sort()   →  is Java's way to IMPLEMENT it
```

### 📦 `java.util.concurrent` Package Hierarchy

```
java.util.concurrent
        │
        ├── ExecutorService                  ← the interface (contract)
        │       │
        │       ├── ThreadPoolExecutor       ← actual implementation under the hood
        │       ├── ScheduledThreadPoolExecutor
        │       └── ForkJoinPool
        │
        └── Executors                        ← factory class (gives you ready-made pools)
                ├── newFixedThreadPool()
                ├── newCachedThreadPool()
                ├── newSingleThreadExecutor()
                └── newScheduledThreadPool()
```

### 🔑 What Each Piece Does

| Component | Type | Role |
|---|---|---|
| **Thread Pool** | Concept | The idea of reusing a fixed set of threads across many tasks |
| **ExecutorService** | Interface | The clean contract you code against — submit, shutdown, get futures |
| **ThreadPoolExecutor** | Class | The actual engine running under the hood when you call `Executors.newFixedThreadPool()` |
| **Executors** | Factory Class | Convenient shortcuts so you don't manually configure `ThreadPoolExecutor` |
| **Future\<T\>** | Interface | Handle to retrieve the result of an async task submitted to the pool |
| **Callable\<T\>** | Interface | A task that returns a value and can throw checked exceptions |

### 💡 Key Insight

When you write:
```java
ExecutorService executor = Executors.newFixedThreadPool(2);
```

What Java **actually** creates under the hood is:
```java
new ThreadPoolExecutor(
    2,                                 // corePoolSize
    2,                                 // maximumPoolSize
    0L, TimeUnit.MILLISECONDS,         // keepAliveTime
    new LinkedBlockingQueue<Runnable>() // task queue
);
```

`Executors` is just a **convenience wrapper** — in production systems, developers often configure `ThreadPoolExecutor` directly for fine-grained control over queue size, rejection policies, and thread lifetimes.

---
## 🍭 The Big Picture

Imagine you run a **small office** with only **2 workers** (threads).

One day, **3 customers** show up with jobs:
- 📋 **Customer A** — needs a 10-second task done
- 📋 **Customer B** — needs a 10-second task done
- 📋 **Customer C** — needs a 60-second task done

Since you only have **2 workers**, the first two customers get served immediately. **Customer C has to wait** in line until one worker finishes their current job.

You (the **main thread / boss**) hand out all the tasks, then sit down and wait for **Task C's result** before going home.

That's exactly what this code does. 🎯

---

## 🧠 Core Concept: Why Not Just Use Raw Threads?

With raw `Thread`, you'd have to:
- Manually create and name every thread
- Manually manage how many run at once
- Manually handle results and errors

**`ExecutorService`** is a **thread pool manager** — it handles all of that for you. You just submit tasks and let it figure out which worker picks them up.

Think of it as the difference between:
- ❌ Hiring individual freelancers one-by-one for every single task
- ✅ Having a **staffing agency** (ExecutorService) that maintains a pool of workers and assigns tasks smartly

---

## 🔍 Code Breakdown — Line by Line

### 1. 🏗️ Creating the Thread Pool

```java
ExecutorService executor = Executors.newFixedThreadPool(2);
```

This creates an **office with exactly 2 permanent workers**.

- If you submit 2 tasks → both workers start immediately
- If you submit a 3rd task → it **waits in a queue** until a worker is free
- The pool size is **fixed** — it never grows or shrinks

**Types of thread pools available:**

| Factory Method | Behaviour |
|---|---|
| `newFixedThreadPool(n)` | Exactly `n` threads, extras wait in queue |
| `newCachedThreadPool()` | Grows/shrinks dynamically, idle threads die after 60s |
| `newSingleThreadExecutor()` | Exactly 1 thread, tasks run one after another |
| `newScheduledThreadPool(n)` | `n` threads, supports delayed/periodic tasks |

---

### 2. 📬 Submitting Tasks — What is `Future`?

```java
Future<?> f       = executor.submit(() -> { /* Task A */ return null; });
Future<Integer> s = executor.submit(() -> { /* Task B */ return 5;    });
Future<Integer> e = executor.submit(() -> { /* Task C */ return 111;  });
```

`executor.submit()` hands a task to the pool and **immediately returns a `Future`**.

A **`Future`** is like a **claim ticket** at a dry cleaner 🧾:
- You drop off your clothes → submit task
- You get a ticket → Future object
- You go do other things
- Later, you come back with the ticket and collect your clothes → `.get()`

The `Future` is a **promise** that a result will be available *eventually*.

**`Future<T>` key methods:**

| Method | What It Does |
|---|---|
| `.get()` | **Blocks** until result is ready, then returns it |
| `.get(timeout, unit)` | Waits only up to timeout, throws `TimeoutException` |
| `.isDone()` | Returns `true` if task is complete (non-blocking) |
| `.isCancelled()` | Returns `true` if task was cancelled |
| `.cancel(true)` | Tries to cancel the task (interrupts if running) |

---

### 3. 📦 Callable vs Runnable

```java
executor.submit(() -> {
    // ... do work ...
    return 5;  // 👈 Callable returns a value
});
```

You're passing a **`Callable`** here, not a `Runnable`.

| | `Runnable` | `Callable<T>` |
|---|---|---|
| Method | `run()` | `call()` |
| Returns value? | ❌ `void` | ✅ Returns `T` |
| Throws checked exception? | ❌ No | ✅ Yes |
| Used with | `Thread`, `executor.execute()` | `executor.submit()` |

> In this code, Task A uses `Callable` returning `null` (declared as `Future<?>`), while Tasks B and C return `Integer` values.

---

### 4. ⏳ What Happens With 3 Tasks and 2 Workers?

```
submit Task A  → Worker-1 picks it up immediately  ✅
submit Task B  → Worker-2 picks it up immediately  ✅
submit Task C  → No worker free! Sits in queue...  ⏳

(After ~10 seconds, Worker-1 OR Worker-2 finishes)
               → That worker picks up Task C        ✅
```

This is the **task queue** behaviour of a fixed thread pool — it uses a `LinkedBlockingQueue` internally.

---

### 5. 🔒 `executor.shutdown()`

```java
executor.shutdown();
```

This tells the executor:
> "Don't accept any NEW tasks, but **finish everything** already submitted."

It does **NOT** kill running tasks. Workers continue until done.

| Method | Behaviour |
|---|---|
| `shutdown()` | Graceful — finish queued + running tasks, no new ones accepted |
| `shutdownNow()` | Aggressive — tries to interrupt running tasks, returns pending list |
| `awaitTermination(n, unit)` | Blocks until all tasks done OR timeout reached |

> ⚠️ Always call `shutdown()`. Without it, your JVM **never exits** because pool threads stay alive.

---

### 6. 🕐 Blocking on `e.get()`

```java
Integer taskCResult = e.get();
System.out.println("Task C Result " + taskCResult);
```

The main thread calls `.get()` on Task C's future.

**What happens step by step:**
1. Task A and B start immediately (2 workers are free)
2. Task C waits in queue (~10 seconds)
3. A worker frees up → Task C starts running (~60 more seconds)
4. Main thread is **blocked on `e.get()`** this entire time
5. Only after Task C finishes (returns `111`) does main thread unblock and print the result

**Timeline:**

```
t=0s  → Task A starts (Worker-1), Task B starts (Worker-2)
t=0s  → Task C queued (no free worker)
t=10s → Task A or B finishes → Task C starts on freed worker
t=70s → Task C finishes → e.get() unblocks → prints "Task C Result 111"
```

---

## 🔁 Full Execution Flow Diagram

```
Main Thread
    │
    ├── Creates ExecutorService (2 workers)
    │
    ├── submit(Task A) ──► Worker-1 starts Task A immediately
    │       └── returns Future f
    │
    ├── submit(Task B) ──► Worker-2 starts Task B immediately
    │       └── returns Future s
    │
    ├── submit(Task C) ──► Queued (no free worker yet)
    │       └── returns Future e
    │
    ├── executor.shutdown()  ← no new tasks, finish existing ones
    │
    ├── e.get() ◄── MAIN THREAD BLOCKS HERE
    │                    │
    │               (t≈10s: A worker finishes Task A or B)
    │               (Task C gets picked up by freed worker)
    │               (t≈70s: Task C returns 111)
    │
    └── Prints "Task C Result 111" → Program exits
```

---

## 🧩 Underlying Concepts Summary

| Concept | What It Means |
|---|---|
| **Thread Pool** | A fixed set of reusable threads; avoids cost of creating/destroying per task |
| **ExecutorService** | Framework that manages thread pools and task submission |
| **Task Queue** | When all workers are busy, new tasks wait here (`LinkedBlockingQueue`) |
| **Future\<T\>** | A handle to an async result — lets you retrieve it later with `.get()` |
| **Callable\<T\>** | Like Runnable, but returns a value and can throw checked exceptions |
| **Blocking call** | `.get()` makes the calling thread wait until the result is ready |
| **Graceful shutdown** | `shutdown()` drains the queue before the pool terminates |

---

## 🎤 Interview Questions & Answers

---

### Q1. What is `ExecutorService` and why use it over raw `Thread`?

`ExecutorService` is a higher-level thread management framework. Raw threads are expensive to create/destroy per task. A thread pool reuses threads, reducing overhead. It also provides task queuing, `Future`-based results, and a clean shutdown mechanism — none of which raw `Thread` provides out of the box.

---

### Q2. What is the difference between `execute()` and `submit()`?

| | `execute(Runnable)` | `submit(Callable/Runnable)` |
|---|---|---|
| Returns | `void` | `Future<T>` |
| Exception handling | Goes to `UncaughtExceptionHandler` | Stored in `Future`, thrown on `.get()` |
| Use when | Fire-and-forget tasks | Tasks that return results or need error handling |

---

### Q3. What happens when you call `Future.get()`?

The calling thread **blocks** until the task completes and the result is available. If the task threw an exception, `.get()` wraps it in `ExecutionException` and rethrows it. Use `.get(timeout, TimeUnit)` to avoid blocking forever.

---

### Q4. What is the difference between `shutdown()` and `shutdownNow()`?

- `shutdown()` — Graceful. Stops accepting new tasks but lets already-submitted ones finish.
- `shutdownNow()` — Forceful. Attempts to interrupt running tasks and returns the list of tasks that never started.

> Best practice: call `shutdown()`, then `awaitTermination()`, then `shutdownNow()` if still not done.

---

### Q5. What is `Callable` and how is it different from `Runnable`?

`Callable<T>` is like `Runnable` but can **return a value** and **throw checked exceptions**. `Runnable.run()` returns void; `Callable.call()` returns `T`. You use `Callable` when you need a result back from a thread.

---

### Q6. What happens to Task C when both workers are busy?

Task C is placed in the executor's **internal task queue** (`LinkedBlockingQueue` for `FixedThreadPool`). It waits there until a worker thread becomes free, then gets picked up automatically. Tasks are served in FIFO order.

---

### Q7. What is `Future.isDone()` vs `Future.get()`?

- `isDone()` is **non-blocking** — returns `true/false` immediately
- `get()` is **blocking** — waits until done and returns the result

Use `isDone()` for polling patterns when you don't want to block the calling thread.

---

### Q8. Can `Future.get()` throw exceptions? Which ones?

Yes. `.get()` declares two checked exceptions:
- `InterruptedException` — if the waiting thread is interrupted while blocking
- `ExecutionException` — if the task itself threw an exception during execution

Always wrap `.get()` in a try-catch for both.

---

### Q9. What is a thread pool and what problem does it solve?

Creating a new `Thread` for every task is expensive (memory allocation, OS scheduling overhead). A **thread pool** pre-creates a fixed number of threads that sit idle and pick up tasks from a queue. This avoids creation/destruction costs and limits the maximum number of concurrent threads, preventing resource exhaustion.

---

### Q10. What is the danger of calling `.get()` without a timeout?

If the task hangs (infinite loop, deadlock, waiting for I/O that never arrives), `.get()` will block the calling thread **forever**. Always prefer `.get(timeout, TimeUnit.SECONDS)` in production code to prevent the main thread from freezing indefinitely.

---

### Q11. Why does the program not exit immediately after `executor.shutdown()`?

`shutdown()` is **non-blocking** — it just signals "no more tasks." The main thread then hits `e.get()` which blocks until Task C finishes (~70s). The JVM won't exit until all **user threads** (including pool threads) are done. So the program lives until Task C completes and pool threads terminate.

---

### Q12. What is `newCachedThreadPool()` and when should you use it vs `newFixedThreadPool()`?

- `newCachedThreadPool()` — creates threads on demand, reuses idle ones, kills idle threads after 60s. Good for **short-lived, bursty tasks** with unpredictable load.
- `newFixedThreadPool(n)` — steady `n` threads, extras queue up. Good for **predictable, CPU-bound workloads** where you want to cap resource usage.

> ⚠️ `newCachedThreadPool()` can spawn **unbounded threads** if tasks pile up — risky under heavy load.

---

### Q13. What is the difference between `Future` and `CompletableFuture`?

| | `Future<T>` | `CompletableFuture<T>` |
|---|---|---|
| Get result | `.get()` (blocking only) | `.get()` OR non-blocking callbacks |
| Chain tasks | ❌ Not possible | ✅ `.thenApply()`, `.thenCompose()` etc. |
| Exception handling | Only via `.get()` catch | `.exceptionally()`, `.handle()` |
| Complete manually | ❌ No | ✅ `.complete(value)` |

`CompletableFuture` is the modern replacement introduced in Java 8 — prefer it for complex async workflows.

---

Made with ❤️ by [@vaibhav25-mnnit](https://github.com/vaibhav25-mnnit)