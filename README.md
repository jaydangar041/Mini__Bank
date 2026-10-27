# MiniBank - All-in-One Java Core Banking System

A comprehensive, industry-grade Core Banking application implemented in a single unified Java file (**[`MiniBank.java`](file:///c:/Users/Jay%20Dangar/Desktop/Mini_Bank/MiniBank.java)**) covering all practical lab requirements from **Lab 1 through Lab 14**.

---

## 📑 Table of Contents
1. [Overview & Project Structure](#overview--project-structure)
2. [Practical-by-Practical Implementation Guide](#practical-by-practical-implementation-guide)
3. [How to Compile and Run](#how-to-compile-and-run)
4. [Answers to Key Questions & Viva Analysis](#answers-to-key-questions--viva-analysis)
5. [Practical Verification Output](#practical-verification-output)

---

## 1. Overview & Project Structure

The entire system is contained in **[`MiniBank.java`](file:///c:/Users/Jay%20Dangar/Desktop/Mini_Bank/MiniBank.java)** and organized into clean, logical layers:

```text
c:\Users\Jay Dangar\Desktop\Mini_Bank\
├── MiniBank.java           # All-in-one source file containing all classes & interfaces
├── minibank.jar            # Packaged executable runnable JAR
├── README.md               # Complete documentation and viva guide
└── data/                   # Persistent runtime directory
    ├── accounts.dat        # Serialized accounts object database
    ├── logs/               # NIO append-only transaction audit logs
    └── reports/            # Generated end-of-day summary reports
```

---

## 2. Practical-by-Practical Implementation Guide

| Practical | Module / Topic | Implemented Components |
|---|---|---|
| **Practical 1** | Menu Shell & CLI | 10-option interactive console loop with validation and clean navigation. |
| **Practical 2** | Encapsulation & Foundational Classes | `Customer` with `customerId` (`CUST101...`), `Account` with `accountNumber` (`AC0001...`), private balance with NO public setter, deposit/withdraw methods, constructor chaining (`this(owner, 0)`). |
| **Practical 3** | Object Methods & Nested Classes | Overridden `toString()`, `equals()` and `hashCode()` based on `accountNumber`; static nested `Customer.Address` class; `Cloneable` deep copy; `instanceof` checks. |
| **Practical 5** | Inheritance & Polymorphism | Abstract `Account` base class with abstract `interestRate()` and `canWithdraw()`; `SavingsAccount` (4.0% interest, minBalance threshold); `CurrentAccount` (0.0% interest, overdraft limit); `FixedDepositAccount` (7.0% interest, maturity lock); `super(...)` constructor calls. |
| **Practical 6** | Interfaces & Functional Contracts | `Transactable` interface; `InterestBearing` with `default double yearlyInterest()` and `halfYearlyInterest()`; `@FunctionalInterface WithdrawRule` used with anonymous classes & lambdas; `Premium` marker interface. |
| **Practical 7** | Metadata & Reflection Validation | Custom annotations `@Id`, `@Positive`, and `@MaxLength`; `AnnotationValidator.validate(Object)` using reflection (`getDeclaredFields`, `setAccessible(true)`). |
| **Practical 8** | Custom Exception Hierarchy | Checked `BankException` base class; `InsufficientFundsException` with `shortfall` property; `InvalidAmountException`, `AccountNotFoundException`, `DailyLimitExceededException`; `try-catch-finally`; `BankSession implements AutoCloseable` for `try-with-resources`. |
| **Practical 9** | Multithreading & Synchronization | `AccountWorker implements Runnable`; race condition demonstration vs synchronized thread safety (10 threads x 1000 deposits = exactly Rs. 10,000). |
| **Practical 10** | Transaction Processing & Concurrency | `TransactionProcessor` with `Executors.newFixedThreadPool(4)`; `TransactionQueue` with `wait()` and `notifyAll()`; deadlock-free transfer using account number lock ordering. |
| **Practical 11** | Serialization & NIO Reports | `Serializable` with `serialVersionUID`; `transient` mutex lock with custom `readObject`; `StatePersister` (`ObjectOutputStream`/`ObjectInputStream`); `TransactionLog` (`Files.write` with `APPEND`); `ReportGenerator` with `Files.newDirectoryStream` & `readAttributes`. |
| **Practical 12** | Collections Framework | `AccountStore` with `HashMap<String, Account>`, `ConcurrentHashMap`, and `ArrayList` transaction history; `sortedByBalance()` with `Comparator`; `Account implements Comparable<Account>` (`compareTo`). |
| **Practical 13** | Generic Repository & Reflection | `Repository<T>` storing `HashMap<String, T>`; reflective key lookup via `@Id`; `@SafeVarargs saveAll(T... items)`; bounded generic method `<N extends Number> double average(N[] values)`. |
| **Practical 14** | Capstone Integration & Test Suite | `BankService` wiring repository, validation, transactions, and audit logging; automated regression verification runner (`runAutomatedPracticalsVerification`). |

---

## 3. How to Compile and Run

### A. Run Interactive Application
```powershell
# Compile the single source file
javac MiniBank.java

# Launch interactive banking console
java MiniBank
```

### B. Run the Automated Practical Verification Suite (Labs 1–14)
```powershell
# Execute all practical test scenarios automatically
java MiniBank --test
```

### C. Build and Run Executable JAR
```powershell
# Package into executable JAR
jar cfe minibank.jar MiniBank *.class

# Run JAR
java -jar minibank.jar
# or run automated tests via JAR:
java -jar minibank.jar --test
```

---

## 4. Answers to Key Questions & Viva Analysis

### Practical 2: Encapsulation
1. **What is encapsulation, and how do private fields with public getters enforce it?**
   - *Answer*: Encapsulation is the object-oriented principle of hiding an object's internal state and requiring all interactions to occur through well-defined public methods. By keeping `balance` `private` with no public setter, external code cannot alter the balance arbitrarily; balance changes can occur only through `deposit()` and `withdraw()`, which enforce domain rules (e.g., rejecting negative amounts or checking available funds).
2. **Why is `accountNumber` declared `final`?**
   - *Answer*: An account number is an immutable identity. Once assigned at creation, it must never change throughout the lifecycle of the object, ensuring consistency and preventing identity tampering.
3. **What is the difference between a static field and an instance field when generating IDs?**
   - *Answer*: An instance field belongs to a specific object instance (each object gets its own copy). A static field belongs to the class itself and is shared among all instances. A static counter increments across all created objects, ensuring sequential, globally unique identifiers like `AC0001`, `AC0002`.

### Practical 3: Object Methods & Nested Classes
1. **Why must `equals()` and `hashCode()` be overridden together?**
   - *Answer*: The `equals`/`hashCode` contract dictates that if two objects are equal according to `equals(Object)`, they **must** return the identical integer from `hashCode()`. If `hashCode()` is not overridden, hash-based collections (`HashMap`, `HashSet`) will place equal objects into different buckets, causing lookup failures.
2. **What does the default `Object.toString()` return, and why override it?**
   - *Answer*: The default returns `getClass().getName() + '@' + Integer.toHexString(hashCode())` (e.g., `SavingsAccount@45ee12a7`). Overriding it provides human-readable text containing meaningful business attributes (`accountNumber | ownerName | balance`).
3. **What is the difference between a static nested class and an inner class?**
   - *Answer*: A static nested class (like `Customer.Address`) does not retain an implicit reference to an enclosing instance and can be instantiated independently (`new Customer.Address(...)`). A non-static inner class is bound to an instance of the outer class and can directly access outer instance fields.

### Practical 5: Inheritance & Polymorphism
1. **What is the difference between an abstract class and a concrete class?**
   - *Answer*: An abstract class cannot be instantiated directly using `new` and can declare abstract methods without bodies. A concrete class provides full implementations for all methods and can be directly instantiated.
2. **What is dynamic method dispatch (run-time polymorphism)?**
   - *Answer*: It is the mechanism by which a call to an overridden method is resolved at runtime rather than compile time. When iterating over an `Account[]` array and invoking `acc.interestRate()`, JVM dynamically invokes `SavingsAccount.interestRate()` or `FixedDepositAccount.interestRate()` based on the actual object type in memory.
3. **What does the `super` keyword do in a subclass constructor?**
   - *Answer*: It calls the constructor of the superclass to properly initialize inherited fields (such as `accountNumber`, `ownerName`, and `balance`) before subclass-specific initialization runs.

### Practical 6: Interfaces
1. **What is the difference between an interface and an abstract class?**
   - *Answer*: A class can implement multiple interfaces but extend only one abstract class. Interfaces define behavioral contracts (and default/static methods), whereas abstract classes can maintain mutable instance state/constructors.
2. **What is a functional interface, and how does a lambda use it?**
   - *Answer*: A functional interface contains exactly one abstract method (annotated with `@FunctionalInterface`). A lambda expression provides an inline, anonymous implementation of that single abstract method without creating a separate named class.
3. **Why is a default method useful in an interface?**
   - *Answer*: Default methods allow library developers to add new methods to existing interfaces without breaking backwards compatibility with existing implementing classes.

### Practical 7: Annotations & Reflection
1. **What are the meta-annotations `@Retention` and `@Target` used for?**
   - *Answer*: `@Retention` defines how long the annotation is retained (`RUNTIME` allows reflection inspection at runtime). `@Target` specifies the Java language elements (e.g., `FIELD`, `METHOD`, `TYPE`) where the annotation can be placed.
2. **What is reflection, and what does `getDeclaredFields()` return?**
   - *Answer*: Reflection is the ability of Java code to inspect and manipulate classes, fields, and methods at runtime. `getDeclaredFields()` returns an array of all fields declared by the class, including private, protected, and default fields (excluding inherited fields).
3. **How do annotations plus reflection let a framework validate any object?**
   - *Answer*: The validator inspects fields generically, checks if constraints like `@Positive` or `@MaxLength` are present, reads the field value via `field.get(obj)`, and checks constraints without needing to know the concrete class type in advance.

### Practical 8: Exceptions
1. **What is the difference between a checked and an unchecked exception?**
   - *Answer*: Checked exceptions (subclasses of `Exception` excluding `RuntimeException`) must be declared in a method's `throws` clause or handled via `try-catch` at compile-time. Unchecked exceptions (`RuntimeException` and `Error`) represent programming bugs or unexpected errors and do not require explicit handling.
2. **What is the purpose of the `finally` block, and when does it run?**
   - *Answer*: The `finally` block is used to execute cleanup code (closing files, releasing locks). It runs regardless of whether an exception was thrown, caught, or not thrown.
3. **How does try-with-resources guarantee a resource is closed?**
   - *Answer*: Any class implementing `AutoCloseable` passed in `try (...)` will automatically have its `close()` method called at the end of the block, even if an exception occurs during execution.

### Practical 9 & 10: Multithreading & Concurrency
1. **What is a race condition, and why does it occur here?**
   - *Answer*: A race condition occurs when multiple threads concurrently read, modify, and write shared data (`balance += amount`). Because `balance += amount` is not an atomic operation (it consists of read, add, write), updates get overwritten.
2. **What does the `synchronized` keyword do?**
   - *Answer*: It acquires an intrinsic lock (monitor) on the target object. Only one thread can execute a synchronized method or block on that object at any given time, guaranteeing mutual exclusion and memory visibility.
3. **What causes a deadlock, and how does consistent lock ordering prevent it?**
   - *Answer*: Deadlock occurs when Thread 1 holds Lock A and waits for Lock B, while Thread 2 holds Lock B and waits for Lock A (circular wait). By enforcing consistent lock ordering (always locking the account with the smaller `accountNumber` first), circular waiting is eliminated and deadlock is prevented.

### Practical 11: Persistence & NIO
1. **What does it mean for a class to be `Serializable`, and why add `serialVersionUID`?**
   - *Answer*: `Serializable` is a marker interface enabling an object graph to be converted into a byte stream and reconstructed. `serialVersionUID` serves as a version stamp to verify that the loaded class definition matches the serialized byte stream.
2. **What is the difference between a byte stream and a character stream?**
   - *Answer*: Byte streams (`InputStream`/`OutputStream`) read and write raw 8-bit bytes (used for binary data, images, serialized objects). Character streams (`Reader`/`Writer`) handle 16-bit Unicode characters and character encodings.
3. **How does NIO's Path/Files API differ from the old File class?**
   - *Answer*: `java.nio.file.Path` and `Files` provide non-blocking support, better metadata inspection (`readAttributes`), structured exception handling, atomic file operations, and scalable directory stream iteration (`newDirectoryStream`).

### Practical 12 & 13: Collections & Generics
1. **What is the difference between a List, a Set, and a Map?**
   - *Answer*: `List` is an ordered collection allowing duplicates. `Set` is an unordered collection containing unique elements. `Map` stores key-value pairs where keys must be unique.
2. **What is the difference between `Comparable` and `Comparator`?**
   - *Answer*: `Comparable` defines the natural sorting order within the class itself via `compareTo()`. `Comparator` is an external strategy passed to sort methods to provide custom ordering criteria (e.g., sorting accounts by balance descending).
3. **What is type erasure?**
   - *Answer*: Type erasure is the compile-time process where the Java compiler replaces generic type parameters with their bounds (or `Object`) and adds casts, allowing generic code to remain backward-compatible with older JVM bytecode.

---

## 5. Verification Check

To run all automated verification tests at any time:
```powershell
java MiniBank --test
```
All 14 practical lab modules execute in sequence and print verification logs confirming correct operation.
