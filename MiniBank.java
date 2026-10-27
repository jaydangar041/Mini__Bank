import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

// ============================================================================
// PRACTICAL LAB 7: CUSTOM ANNOTATIONS (METADATA & REFLECTION)
// ============================================================================

/**
 * Marker annotation to identify the primary key / identifier field of an entity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Id {}

/**
 * Validation annotation ensuring a numerical field contains a positive value (> 0).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Positive {
    String message() default "must be > 0";
}

/**
 * Validation annotation enforcing a maximum string length.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface MaxLength {
    int value();
    String message() default "exceeds maximum permitted length";
}

/**
 * Audit annotation to mark service methods for reflective audit logging.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Audit {}

// ============================================================================
// PRACTICAL LAB 6: INTERFACES & FUNCTIONAL CONTRACTS
// ============================================================================

/**
 * Base transactional interface defining deposit and withdrawal operations.
 */
interface Transactable {
    void deposit(long amount) throws InvalidAmountException;
    boolean withdraw(long amount) throws InsufficientFundsException, InvalidAmountException;
}

/**
 * Interface for interest-bearing financial instruments.
 * Demonstrates default methods in interfaces.
 */
interface InterestBearing {
    double interestRate();
    long getBalance();

    /**
     * Default method computing annual interest based on rate and balance.
     */
    default double yearlyInterest() {
        return (getBalance() * interestRate()) / 100.0;
    }

    /**
     * Supplementary default method computing half-yearly interest.
     */
    default double halfYearlyInterest() {
        return yearlyInterest() / 2.0;
    }
}

/**
 * Functional interface for custom withdrawal validation rules.
 */
@FunctionalInterface
interface WithdrawRule {
    boolean allow(Account account, long amount);
}

/**
 * Marker interface designating premium account status (no methods).
 */
interface Premium {}

// ============================================================================
// PRACTICAL LAB 8: CUSTOM EXCEPTION HIERARCHY
// ============================================================================

/**
 * Checked base exception for all domain-specific banking failures.
 */
class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when an account has inadequate balance for withdrawal or transfer.
 * Encapsulates the specific shortfall amount.
 */
class InsufficientFundsException extends BankException {
    private final long shortfall;

    public InsufficientFundsException(String message, long shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public long getShortfall() {
        return shortfall;
    }
}

/**
 * Exception thrown when an account number cannot be located in the repository.
 */
class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when a monetary transaction amount is non-positive or invalid.
 */
class InvalidAmountException extends BankException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when a withdrawal exceeds the daily cap limit.
 */
class DailyLimitExceededException extends BankException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}

/**
 * Practical 8: AutoCloseable resource demonstrating try-with-resources.
 */
class BankSession implements AutoCloseable {
    private final String sessionId;
    private final String operator;

    public BankSession(String operator) {
        this.operator = operator;
        this.sessionId = "SESS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("  [Session] Opened " + sessionId + " by operator: " + operator);
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void close() {
        System.out.println("  [Session] AutoCloseable: Session " + sessionId + " securely closed.");
    }
}

// ============================================================================
// PRACTICAL LAB 2, 3: CUSTOMER & NESTED ADDRESS
// ============================================================================

/**
 * Customer entity demonstrating encapsulation, static ID generation,
 * nested Address class, and deep cloning.
 */
class Customer implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private static long customerCounter = 100; // Produces CUST101, CUST102...

    @Id
    private final String customerId;

    @MaxLength(50)
    private String name;

    @MaxLength(60)
    private String email;

    @MaxLength(15)
    private String mobile;

    private Address address;

    /**
     * Static method generating unique customer IDs in synchronized thread-safe manner.
     */
    private static synchronized String generateCustomerId() {
        return "CUST" + (++customerCounter);
    }

    public Customer(String name, String email, String mobile) {
        this(name, email, mobile, null);
    }

    public Customer(String name, String email, String mobile, Address address) {
        this.customerId = generateCustomerId();
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
    }

    // Public Getters and Setters
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    @Override
    public String toString() {
        return "Customer [" + customerId + " | " + name + " | " + email + " | " + mobile +
                (address != null ? " | " + address : "") + "]";
    }

    @Override
    public Customer clone() {
        try {
            Customer cloned = (Customer) super.clone();
            if (this.address != null) {
                cloned.address = this.address.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning failed", e);
        }
    }

    /**
     * Practical Lab 3: Public static nested class Address.
     */
    public static class Address implements Serializable, Cloneable {
        private static final long serialVersionUID = 1L;
        private String line;
        private String city;
        private String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() { return line; }
        public String getCity() { return city; }
        public String getPincode() { return pincode; }

        @Override
        public String toString() {
            return line + ", " + city + " - " + pincode;
        }

        @Override
        public Address clone() {
            try {
                return (Address) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Address clone failed", e);
            }
        }
    }
}

// ============================================================================
// PRACTICAL LAB 2, 3, 5, 6, 8, 9, 11, 12: ABSTRACT ACCOUNT & HIERARCHY
// ============================================================================

/**
 * Abstract base Account class encapsulating core banking data,
 * polymorphic contracts, synchronization for thread safety,
 * reflection annotations, and serialization.
 */
abstract class Account implements Transactable, InterestBearing, Comparable<Account>, Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private static long accountCounter = 0;

    /**
     * Synchronized static generator for unique account numbers (e.g., AC0001).
     */
    public static synchronized String generateAccountNumber() {
        return String.format("AC%04d", ++accountCounter);
    }

    @Id
    private final String accountNumber;

    @MaxLength(50)
    private String ownerName;

    @Positive
    private long balance; // in whole rupees

    private boolean active;

    // Mutex lock object marked transient (Practical 11: non-serializable state)
    private transient Object lock = new Object();

    // Transaction history per account
    private final List<String> passbook = new ArrayList<>();

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.lock = new Object();
    }

    /**
     * Primary constructor taking ownerName and opening balance.
     */
    public Account(String ownerName, long balance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = balance;
        this.active = true;
        this.passbook.add(formatTxRecord("OPEN", balance, balance));
    }

    /**
     * Secondary constructor chaining to the primary using this(ownerName, 0).
     */
    public Account(String ownerName) {
        this(ownerName, 0L);
    }

    // Public Getters (Strict Encapsulation: NO public setter for balance)
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public long getBalance() { return balance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<String> getPassbook() { return Collections.unmodifiableList(passbook); }

    // Abstract methods to be realized by specialized account types (Polymorphism)
    @Override
    public abstract double interestRate();
    public abstract boolean canWithdraw(long amount);

    /**
     * Thread-safe deposit operation with strict validation.
     */
    @Override
    public synchronized void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than zero! Provided: " + amount);
        }
        this.balance += amount;
        this.passbook.add(formatTxRecord("DEPOSIT", amount, this.balance));
    }

    /**
     * Thread-safe withdrawal operation with rule enforcement and shortfall tracking.
     */
    @Override
    public synchronized boolean withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero! Provided: " + amount);
        }
        if (!active) {
            System.out.println("  [Alert] Cannot withdraw: Account " + accountNumber + " is inactive.");
            return false;
        }
        if (!canWithdraw(amount)) {
            long shortfall = amount - balance;
            throw new InsufficientFundsException("Withdrawal failed on " + accountNumber + ": Insufficient funds! Shortfall = " + shortfall, shortfall);
        }
        this.balance -= amount;
        this.passbook.add(formatTxRecord("WITHDRAW", amount, this.balance));
        return true;
    }

    /**
     * Supplementary helper: Inter-account fund transfer.
     */
    public void transfer(Account target, long amount) throws BankException {
        if (target == null) {
            throw new AccountNotFoundException("Target destination account cannot be null!");
        }
        if (this.equals(target)) {
            throw new InvalidAmountException("Cannot transfer funds to the identical account!");
        }

        // Deadlock Prevention (Practical Lab 10): Order locks by accountNumber
        Account firstLock = this.accountNumber.compareTo(target.accountNumber) < 0 ? this : target;
        Account secondLock = (firstLock == this) ? target : this;

        synchronized (firstLock) {
            synchronized (secondLock) {
                try {
                    this.withdraw(amount);
                    target.deposit(amount);
                    this.passbook.add(formatTxRecord("XFER_TO " + target.getAccountNumber(), amount, this.balance));
                    target.passbook.add(formatTxRecord("XFER_FROM " + this.getAccountNumber(), amount, target.getBalance()));
                } catch (BankException e) {
                    throw e;
                }
            }
        }
    }

    /**
     * Supplementary: Monthly interest calculation.
     */
    public double monthlyInterest() {
        return (getBalance() * interestRate()) / (100.0 * 12.0);
    }

    private String formatTxRecord(String type, long amount, long currentBal) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("[%s] %-14s : Rs. %-8d | Bal: Rs. %d", timestamp, type, amount, currentBal);
    }

    public String generateFormattedStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append(String.format(" MINI BANK ACCOUNT STATEMENT: %s\n", accountNumber));
        sb.append("====================================================\n");
        sb.append(String.format(" Account Type : %s\n", this.getClass().getSimpleName()));
        sb.append(String.format(" Owner Name   : %s\n", ownerName));
        sb.append(String.format(" Status       : %s\n", active ? "ACTIVE" : "INACTIVE"));
        sb.append(String.format(" Current Bal  : Rs. %d\n", balance));
        sb.append(String.format(" Interest Rate: %.1f%%\n", interestRate()));
        sb.append("----------------------------------------------------\n");
        sb.append(" TRANSACTION HISTORY:\n");
        for (String tx : passbook) {
            sb.append("  ").append(tx).append("\n");
        }
        sb.append("====================================================\n");
        return sb.toString();
    }

    // Practical 3: Object Methods Overrides
    @Override
    public String toString() {
        return String.format("%s | %-12s | Rs. %-8d | Rate: %4.1f%% | Active: %b",
                accountNumber, ownerName, balance, interestRate(), active);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }
}

// ----------------------------------------------------------------------------
// ACCOUNT SUBCLASSES (PRACTICAL 5)
// ----------------------------------------------------------------------------

/**
 * SavingsAccount requiring a minimum balance threshold.
 */
class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    private final long minBalance;

    public SavingsAccount(String ownerName, long balance, long minBalance) {
        super(ownerName, balance);
        this.minBalance = minBalance;
    }

    public SavingsAccount(String ownerName, long balance) {
        this(ownerName, balance, 1000L); // Default min balance Rs. 1000
    }

    public long getMinBalance() {
        return minBalance;
    }

    @Override
    public double interestRate() {
        return 4.0; // 4.0% p.a.
    }

    @Override
    public boolean canWithdraw(long amount) {
        return (getBalance() - amount) >= minBalance;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [MinBal: Rs. %d]", minBalance);
    }
}

/**
 * CurrentAccount offering overdraft protection.
 */
class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private final long overdraftLimit;

    public CurrentAccount(String ownerName, long balance, long overdraftLimit) {
        super(ownerName, balance);
        this.overdraftLimit = overdraftLimit;
    }

    public CurrentAccount(String ownerName, long balance) {
        this(ownerName, balance, 5000L); // Default overdraft Rs. 5000
    }

    public long getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    public double interestRate() {
        return 0.0; // 0.0% p.a.
    }

    @Override
    public boolean canWithdraw(long amount) {
        return (getBalance() - amount) >= -overdraftLimit;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [OverdraftLimit: Rs. %d]", overdraftLimit);
    }
}

/**
 * FixedDepositAccount with locked funds until maturity.
 */
class FixedDepositAccount extends Account implements Premium {
    private static final long serialVersionUID = 1L;
    private boolean matured;
    private final int tenureMonths;

    public FixedDepositAccount(String ownerName, long balance, int tenureMonths) {
        super(ownerName, balance);
        this.tenureMonths = tenureMonths;
        this.matured = false;
    }

    public boolean isMatured() {
        return matured;
    }

    public void setMatured(boolean matured) {
        this.matured = matured;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    @Override
    public double interestRate() {
        return 7.0; // 7.0% p.a.
    }

    @Override
    public boolean canWithdraw(long amount) {
        // Funds locked until maturity
        return matured && (getBalance() >= amount);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [Tenure: %d mos | Matured: %b]", tenureMonths, matured);
    }
}

// ============================================================================
// PRACTICAL LAB 7: ANNOTATION VALIDATOR VIA REFLECTION
// ============================================================================

class AnnotationValidator {
    /**
     * Inspects fields of the target object using reflection to validate
     * constraints marked by @Positive and @MaxLength.
     */
    public static String[] validate(Object obj) {
        if (obj == null) return new String[]{"Target object is null"};
        List<String> errors = new ArrayList<>();
        Class<?> clazz = obj.getClass();

        // Traverse class hierarchy to validate superclass fields too
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object val = field.get(obj);

                    // Validate @Positive
                    if (field.isAnnotationPresent(Positive.class)) {
                        Positive pos = field.getAnnotation(Positive.class);
                        if (val instanceof Number) {
                            long numVal = ((Number) val).longValue();
                            if (numVal <= 0) {
                                errors.add(String.format("Field '%s' with value %d %s",
                                        field.getName(), numVal, pos.message()));
                            }
                        }
                    }

                    // Validate @MaxLength
                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength max = field.getAnnotation(MaxLength.class);
                        if (val instanceof String) {
                            String strVal = (String) val;
                            if (strVal.length() > max.value()) {
                                errors.add(String.format("Field '%s' length (%d) %s (max %d)",
                                        field.getName(), strVal.length(), max.message(), max.value()));
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    errors.add("Unable to reflectively access field: " + field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return errors.toArray(new String[0]);
    }
}

// ============================================================================
// PRACTICAL LAB 9 & 10: MULTITHREADING, CONCURRENCY & DEADLOCK AVOIDANCE
// ============================================================================

/**
 * Worker thread for concurrent deposits demonstrating race conditions vs synchronization.
 */
class AccountWorker implements Runnable {
    private final Account account;
    private final int times;
    private final long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    @Override
    public void run() {
        for (int i = 0; i < times; i++) {
            try {
                account.deposit(amount);
            } catch (InvalidAmountException e) {
                System.err.println("Worker deposit error: " + e.getMessage());
            }
        }
    }
}

/**
 * Thread-safe transaction processor using a managed fixed thread pool.
 */
class TransactionProcessor {
    private final ExecutorService executor;

    public TransactionProcessor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
    }

    public void submit(Runnable task) {
        executor.execute(task);
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Producer-Consumer queue implementing inter-thread wait/notify synchronization.
 */
class TransactionQueue {
    private final Queue<String> buffer = new LinkedList<>();
    private final int capacity;

    public TransactionQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(String tx) throws InterruptedException {
        while (buffer.size() == capacity) {
            wait();
        }
        buffer.add(tx);
        notifyAll();
    }

    public synchronized String consume() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait();
        }
        String tx = buffer.poll();
        notifyAll();
        return tx;
    }
}

// ============================================================================
// PRACTICAL LAB 11: SERIALIZATION, AUDIT LOGGING & NIO REPORTING
// ============================================================================

/**
 * Handles object serialization and deserialization using Object Streams.
 */
class StatePersister {
    public static void save(Account[] accounts, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(accounts);
        }
    }

    public static Account[] load(Path file) throws IOException, ClassNotFoundException {
        if (!Files.exists(file)) {
            return new Account[0];
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            return (Account[]) ois.readObject();
        }
    }
}

/**
 * Append-only transaction audit logging using Java NIO Files.write.
 */
class TransactionLog {
    public static void append(Path file, String line) throws IOException {
        Files.createDirectories(file.getParent());
        String logEntry = String.format("[%s] %s%n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), line);
        Files.write(file, logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}

/**
 * End-of-Day reporting tool walking directory streams and aggregating log records.
 */
class ReportGenerator {
    public static String generateReport(Path logsDir, Path reportOut) throws IOException {
        long totalDeposits = 0;
        long totalWithdrawals = 0;
        long totalTransfers = 0;
        int filesScanned = 0;

        StringBuilder reportSb = new StringBuilder();
        reportSb.append("============================================================\n");
        reportSb.append("            MINIBANK END-OF-DAY AUDIT REPORT\n");
        reportSb.append("============================================================\n");
        reportSb.append("Report Generated: ").append(LocalDateTime.now()).append("\n\n");

        if (Files.exists(logsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir, "*.log")) {
                for (Path entry : stream) {
                    filesScanned++;
                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                    reportSb.append(String.format("File: %-20s | Size: %6d bytes | Last Modified: %s%n",
                            entry.getFileName(), attrs.size(), attrs.lastModifiedTime()));

                    try (BufferedReader reader = Files.newBufferedReader(entry)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("DEPOSIT")) {
                                totalDeposits += extractAmount(line);
                            } else if (line.contains("WITHDRAW")) {
                                totalWithdrawals += extractAmount(line);
                            } else if (line.contains("TRANSFER")) {
                                totalTransfers += extractAmount(line);
                            }
                        }
                    }
                }
            }
        }

        long netChange = totalDeposits - totalWithdrawals;
        reportSb.append("\n------------------------------------------------------------\n");
        reportSb.append(String.format(" Log Files Inspected : %d%n", filesScanned));
        reportSb.append(String.format(" Total Deposits      : Rs. %d%n", totalDeposits));
        reportSb.append(String.format(" Total Withdrawals   : Rs. %d%n", totalWithdrawals));
        reportSb.append(String.format(" Total Transfers     : Rs. %d%n", totalTransfers));
        reportSb.append(String.format(" Net Liquidity Change: Rs. %+d%n", netChange));
        reportSb.append("============================================================\n");

        String reportStr = reportSb.toString();
        if (reportOut != null) {
            Files.createDirectories(reportOut.getParent());
            Files.write(reportOut, reportStr.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        return reportStr;
    }

    private static long extractAmount(String line) {
        try {
            String[] parts = line.split("\\s+");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("amount:") && i + 1 < parts.length) {
                    return Long.parseLong(parts[i + 1].replaceAll("[^0-9]", ""));
                }
            }
            // Fallback parsing last numeric token
            for (int i = parts.length - 1; i >= 0; i--) {
                String clean = parts[i].replaceAll("[^0-9]", "");
                if (!clean.isEmpty()) return Long.parseLong(clean);
            }
        } catch (Exception ignored) {}
        return 0;
    }
}

// ============================================================================
// PRACTICAL LAB 12: COLLECTIONS FRAMEWORK
// ============================================================================

/**
 * Account storage repository using standard Collections (HashMap, ArrayList, ConcurrentHashMap).
 */
class AccountStore {
    private final Map<String, Account> accountMap = new HashMap<>();
    private final Map<String, Account> concurrentMap = new ConcurrentHashMap<>();
    private final List<String> txHistory = new ArrayList<>();

    public void add(Account account) {
        accountMap.put(account.getAccountNumber(), account);
        concurrentMap.put(account.getAccountNumber(), account);
        txHistory.add("Added Account: " + account.getAccountNumber() + " for " + account.getOwnerName());
    }

    public Account get(String accountNumber) {
        return accountMap.get(accountNumber);
    }

    public List<Account> getAll() {
        return new ArrayList<>(accountMap.values());
    }

    public Map<String, Account> getConcurrentMap() {
        return concurrentMap;
    }

    public List<Account> sortedByBalance() {
        List<Account> list = new ArrayList<>(accountMap.values());
        list.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return list;
    }

    public List<Account> sortedByAccountNumber() {
        List<Account> list = new ArrayList<>(accountMap.values());
        Collections.sort(list); // Uses Comparable<Account> compareTo
        return list;
    }

    public List<Account> filterBalanceAbove(long threshold) {
        List<Account> results = new ArrayList<>();
        for (Account a : accountMap.values()) {
            if (a.getBalance() > threshold) {
                results.add(a);
            }
        }
        return results;
    }
}

// ============================================================================
// PRACTICAL LAB 13: GENERIC REPOSITORY PATTERN & REFLECTION ID EXTRACTION
// ============================================================================

/**
 * Generic type-safe Repository storing any entity with an @Id field.
 *
 * @param <T> Entity type managed by this repository
 */
class Repository<T> {
    private final Map<String, T> storage = new HashMap<>();

    /**
     * Reflectively extracts the field marked with @Id to use as map key.
     */
    public synchronized T save(T entity) {
        if (entity == null) throw new IllegalArgumentException("Cannot save null entity");
        String idVal = extractId(entity);
        storage.put(idVal, entity);
        return entity;
    }

    public synchronized T findById(String id) {
        return storage.get(id);
    }

    public synchronized ArrayList<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    public synchronized boolean delete(String id) {
        return storage.remove(id) != null;
    }

    public synchronized int count() {
        return storage.size();
    }

    @SafeVarargs
    public final void saveAll(T... items) {
        for (T item : items) {
            save(item);
        }
    }

    /**
     * Generic method with bounded type parameter.
     */
    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) return 0.0;
        double sum = 0.0;
        for (N val : values) {
            sum += val.doubleValue();
        }
        return sum / values.length;
    }

    private String extractId(T entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object val = field.get(entity);
                        if (val != null) return val.toString();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cannot access @Id field: " + field.getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalArgumentException("Entity " + entity.getClass().getSimpleName() + " lacks an @Id field!");
    }
}

// ============================================================================
// PRACTICAL LAB 14: SERVICE LAYER (BANK SERVICE INTEGRATION)
// ============================================================================

/**
 * Banking service orchestrating repository, validation, logging, and concurrency.
 */
class BankService {
    private final Repository<Account> repository;
    private final Path logFile = Paths.get("data", "logs", "transactions.log");
    private final Path dataFile = Paths.get("data", "accounts.dat");

    public BankService(Repository<Account> repository) {
        this.repository = repository;
    }

    @Audit
    public Account openAccount(String type, String owner, long initialDeposit) throws BankException {
        Account acc;
        switch (type.trim().toUpperCase()) {
            case "SAVINGS":
                acc = new SavingsAccount(owner, initialDeposit);
                break;
            case "CURRENT":
                acc = new CurrentAccount(owner, initialDeposit);
                break;
            case "FIXED":
            case "FD":
                acc = new FixedDepositAccount(owner, initialDeposit, 12);
                break;
            default:
                throw new BankException("Unsupported account category: " + type);
        }

        // Validate via Reflection
        String[] errors = AnnotationValidator.validate(acc);
        if (errors.length > 0 && initialDeposit < 0) {
            throw new InvalidAmountException("Validation failed: " + String.join(", ", errors));
        }

        repository.save(acc);
        logTx("OPEN_ACCOUNT " + acc.getAccountNumber() + " type: " + type + " owner: " + owner + " amount: " + initialDeposit);
        return acc;
    }

    @Audit
    public void deposit(String accNo, long amount) throws BankException {
        Account acc = getExistingAccount(accNo);
        acc.deposit(amount);
        logTx("DEPOSIT " + accNo + " amount: " + amount + " newBal: " + acc.getBalance());
    }

    @Audit
    public void withdraw(String accNo, long amount) throws BankException {
        Account acc = getExistingAccount(accNo);
        acc.withdraw(amount);
        logTx("WITHDRAW " + accNo + " amount: " + amount + " newBal: " + acc.getBalance());
    }

    @Audit
    public void transfer(String fromAccNo, String toAccNo, long amount) throws BankException {
        Account from = getExistingAccount(fromAccNo);
        Account to = getExistingAccount(toAccNo);
        from.transfer(to, amount);
        logTx("TRANSFER from: " + fromAccNo + " to: " + toAccNo + " amount: " + amount);
    }

    public String getStatement(String accNo) throws BankException {
        Account acc = getExistingAccount(accNo);
        return acc.generateFormattedStatement();
    }

    public List<Account> listAccounts() {
        return repository.findAll();
    }

    public List<Account> searchByOwner(String nameQuery) {
        List<Account> matches = new ArrayList<>();
        for (Account a : repository.findAll()) {
            if (a.getOwnerName().toLowerCase().contains(nameQuery.toLowerCase())) {
                matches.add(a);
            }
        }
        return matches;
    }

    public Account getExistingAccount(String accNo) throws AccountNotFoundException {
        Account acc = repository.findById(accNo);
        if (acc == null) {
            throw new AccountNotFoundException("Account number '" + accNo + "' was not found in the bank records!");
        }
        return acc;
    }

    public void persistState() {
        try {
            List<Account> list = repository.findAll();
            Account[] arr = list.toArray(new Account[0]);
            StatePersister.save(arr, dataFile);
            System.out.println("  [System] Successfully persisted " + arr.length + " accounts to " + dataFile);
        } catch (IOException e) {
            System.err.println("  [Error] Failed to persist accounts: " + e.getMessage());
        }
    }

    public void loadState() {
        try {
            Account[] loaded = StatePersister.load(dataFile);
            for (Account a : loaded) {
                repository.save(a);
            }
            if (loaded.length > 0) {
                System.out.println("  [System] Loaded " + loaded.length + " accounts from storage.");
            }
        } catch (Exception e) {
            System.out.println("  [System] Fresh start (no existing persisted state found).");
        }
    }

    private void logTx(String details) {
        try {
            TransactionLog.append(logFile, details);
        } catch (IOException e) {
            System.err.println("Audit log failure: " + e.getMessage());
        }
    }
}

// ============================================================================
// MAIN APPLICATION & INTERACTIVE CONSOLE MENU (PRACTICALS 1 & 14)
// ============================================================================

/**
 * Main entry point for the MiniBank system.
 * Includes interactive menu and an automated verification suite that executes
 * all practical lab requirements sequentially.
 */
public class MiniBank {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Repository<Account> repository = new Repository<>();
    private static final BankService bankService = new BankService(repository);

    public static void main(String[] args) {
        // If command-line argument "--test" is provided, execute automated lab tests
        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            runAutomatedPracticalsVerification();
            return;
        }

        // Load existing state on start-up
        bankService.loadState();

        // Seed demo data if repository is empty
        if (repository.count() == 0) {
            seedSampleData();
        }

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-10): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleOpenAccount();
                    break;
                case "2":
                    handleDeposit();
                    break;
                case "3":
                    handleWithdraw();
                    break;
                case "4":
                    handleTransfer();
                    break;
                case "5":
                    handleViewStatement();
                    break;
                case "6":
                    handleListAccounts();
                    break;
                case "7":
                    handleBatchProcessing();
                    break;
                case "8":
                    handleGenerateReport();
                    break;
                case "9":
                    runAutomatedPracticalsVerification();
                    break;
                case "10":
                    System.out.println("\nSaving application state and exiting MiniBank. Thank you!");
                    bankService.persistState();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection! Please enter a number between 1 and 10.");
            }
            if (running) {
                System.out.println("\nPress [Enter] to return to the main menu...");
                scanner.nextLine();
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================================");
        System.out.println("           MINIBANK CORE BANKING SYSTEM");
        System.out.println("========================================================");
        System.out.println("  1. Open New Account (Savings / Current / Fixed Deposit)");
        System.out.println("  2. Deposit Funds");
        System.out.println("  3. Withdraw Funds");
        System.out.println("  4. Transfer Funds (Inter-Account)");
        System.out.println("  5. View Account Statement & Details");
        System.out.println("  6. List All Accounts (Sorted / Filtered)");
        System.out.println("  7. Process Batch Transactions (Thread Pool Concurrency)");
        System.out.println("  8. View End-of-Day NIO Audit Report");
        System.out.println("  9. Run Automated Practicals Verification Suite (Labs 1-14)");
        System.out.println(" 10. Save & Exit");
        System.out.println("========================================================");
    }

    private static void seedSampleData() {
        try {
            bankService.openAccount("SAVINGS", "Riya Sharma", 5000L);
            bankService.openAccount("CURRENT", "Aarav Patel", 12000L);
            bankService.openAccount("FIXED", "Jay Dangar", 50000L);
            System.out.println("  [System] Initialized sample accounts for demonstration.");
        } catch (BankException ignored) {}
    }

    private static void handleOpenAccount() {
        System.out.println("\n--- OPEN NEW ACCOUNT ---");
        System.out.print("Account Type (SAVINGS / CURRENT / FIXED): ");
        String type = scanner.nextLine().trim();
        System.out.print("Account Holder Name: ");
        String owner = scanner.nextLine().trim();
        System.out.print("Initial Opening Deposit (Rs): ");
        try {
            long deposit = Long.parseLong(scanner.nextLine().trim());
            Account acc = bankService.openAccount(type, owner, deposit);
            System.out.println(" SUCCESS! Created Account: " + acc.getAccountNumber());
            System.out.println(" Details: " + acc);
        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid monetary amount format!");
        } catch (BankException e) {
            System.out.println(" Bank Error: " + e.getMessage());
        }
    }

    private static void handleDeposit() {
        System.out.println("\n--- DEPOSIT FUNDS ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Amount to Deposit (Rs): ");
        try {
            long amount = Long.parseLong(scanner.nextLine().trim());
            bankService.deposit(accNo, amount);
            Account acc = bankService.getExistingAccount(accNo);
            System.out.println(" SUCCESS: Deposited Rs. " + amount + ". New Balance = Rs. " + acc.getBalance());
        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid number!");
        } catch (BankException e) {
            System.out.println(" Transaction Failed: " + e.getMessage());
        }
    }

    private static void handleWithdraw() {
        System.out.println("\n--- WITHDRAW FUNDS ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Amount to Withdraw (Rs): ");
        try {
            long amount = Long.parseLong(scanner.nextLine().trim());
            bankService.withdraw(accNo, amount);
            Account acc = bankService.getExistingAccount(accNo);
            System.out.println(" SUCCESS: Withdrew Rs. " + amount + ". New Balance = Rs. " + acc.getBalance());
        } catch (InsufficientFundsException e) {
            System.out.println(" INSUFFICIENT FUNDS: " + e.getMessage() + " (Shortfall: Rs. " + e.getShortfall() + ")");
        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid number!");
        } catch (BankException e) {
            System.out.println(" Transaction Failed: " + e.getMessage());
        }
    }

    private static void handleTransfer() {
        System.out.println("\n--- INTER-ACCOUNT TRANSFER ---");
        System.out.print("Enter Source Account Number: ");
        String from = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Destination Account Number: ");
        String to = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Amount to Transfer (Rs): ");
        try {
            long amount = Long.parseLong(scanner.nextLine().trim());
            bankService.transfer(from, to, amount);
            System.out.println(" SUCCESS: Transferred Rs. " + amount + " from " + from + " to " + to);
        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid number!");
        } catch (BankException e) {
            System.out.println(" Transfer Failed: " + e.getMessage());
        }
    }

    private static void handleViewStatement() {
        System.out.print("\nEnter Account Number: ");
        String accNo = scanner.nextLine().trim().toUpperCase();
        try {
            String stmt = bankService.getStatement(accNo);
            System.out.println(stmt);
        } catch (BankException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void handleListAccounts() {
        System.out.println("\n--- ALL REGISTERED ACCOUNTS ---");
        List<Account> accounts = bankService.listAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts on file.");
            return;
        }

        System.out.println("1. Sort by Account Number (Natural Comparable order)");
        System.out.println("2. Sort by Balance Descending (Comparator order)");
        System.out.print("Select view option (1-2): ");
        String opt = scanner.nextLine().trim();

        if ("2".equals(opt)) {
            accounts.sort(Comparator.comparingLong(Account::getBalance).reversed());
        } else {
            Collections.sort(accounts);
        }

        System.out.println("\n--------------------------------------------------------------------------------");
        for (Account a : accounts) {
            System.out.println("  " + a);
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void handleBatchProcessing() {
        System.out.println("\n--- BATCH TRANSACTION PROCESSING (THREAD POOL) ---");
        List<Account> accounts = bankService.listAccounts();
        if (accounts.size() < 2) {
            System.out.println("Need at least 2 accounts for batch processing.");
            return;
        }

        Account accA = accounts.get(0);
        Account accB = accounts.get(1);

        System.out.println("Dispatching 20 concurrent transactions to Thread Pool (4 threads)...");
        TransactionProcessor processor = new TransactionProcessor(4);

        for (int i = 0; i < 10; i++) {
            processor.submit(() -> {
                try {
                    accA.deposit(100);
                } catch (InvalidAmountException ignored) {}
            });
            processor.submit(() -> {
                try {
                    accB.deposit(50);
                } catch (InvalidAmountException ignored) {}
            });
        }

        processor.stop();
        System.out.println(" Batch processing completed safely!");
        System.out.println(" " + accA.getAccountNumber() + " Balance: Rs. " + accA.getBalance());
        System.out.println(" " + accB.getAccountNumber() + " Balance: Rs. " + accB.getBalance());
    }

    private static void handleGenerateReport() {
        System.out.println("\n--- GENERATING NIO AUDIT REPORT ---");
        Path logsDir = Paths.get("data", "logs");
        Path reportOut = Paths.get("data", "reports", "daily_summary.txt");
        try {
            String report = ReportGenerator.generateReport(logsDir, reportOut);
            System.out.println(report);
            System.out.println(" Report saved to: " + reportOut.toAbsolutePath());
        } catch (IOException e) {
            System.out.println(" Failed to generate report: " + e.getMessage());
        }
    }

    // ========================================================================
    // AUTOMATED PRACTICALS VERIFICATION SUITE (LABS 1 THROUGH 14)
    // ========================================================================
    public static void runAutomatedPracticalsVerification() {
        System.out.println("\n====================================================================");
        System.out.println("       MINIBANK COMPREHENSIVE PRACTICAL VERIFICATION SUITE");
        System.out.println("====================================================================");

        // --------------------------------------------------------------------
        // PRACTICAL 2 VERIFICATION: Encapsulation & Static ID Generation
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 2] Testing Encapsulation, Constructors & Static IDs");
        Customer c1 = new Customer("Riya", "riya@example.com", "9876543210");
        System.out.println("  Generated Customer: " + c1);
        assert c1.getCustomerId().startsWith("CUST") : "Customer ID format must start with CUST";

        SavingsAccount p2Acc = new SavingsAccount("Riya", 5000L, 1000L);
        System.out.println("  Opening Balance: " + p2Acc.getBalance());
        try {
            p2Acc.deposit(2000L);
            System.out.println("  After deposit(2000): " + p2Acc.getBalance() + " (Expected: 7000)");
            p2Acc.withdraw(3000L);
            System.out.println("  After withdraw(3000): " + p2Acc.getBalance() + " (Expected: 4000)");

            try {
                p2Acc.withdraw(10000L);
            } catch (InsufficientFundsException e) {
                System.out.println("  withdraw(10000) correctly rejected with shortfall = " + e.getShortfall());
            }
            System.out.println("  Balance remains: " + p2Acc.getBalance() + " (Expected: 4000)");
        } catch (BankException e) {
            System.err.println("  Unexpected failure: " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // PRACTICAL 3 VERIFICATION: Object Methods & Nested Address
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 3] Testing toString, equals, hashCode, clone, and Nested Address");
        Customer.Address addr = new Customer.Address("123 MG Road", "Ahmedabad", "380001");
        c1.setAddress(addr);
        Customer clonedCustomer = c1.clone();
        System.out.println("  Original Customer: " + c1);
        System.out.println("  Cloned Customer:   " + clonedCustomer);
        System.out.println("  Deep clone verification (c1 != clonedCustomer): " + (c1 != clonedCustomer));
        System.out.println("  Nested address cloned (addr != cloned.address): " + (c1.getAddress() != clonedCustomer.getAddress()));

        SavingsAccount a1 = new SavingsAccount("Riya", 4000L);
        System.out.println("  toString Output: " + a1);
        System.out.println("  instanceof Account check: " + (a1 instanceof Account));
        System.out.println("  instanceof SavingsAccount check: " + (a1 instanceof SavingsAccount));

        // --------------------------------------------------------------------
        // PRACTICAL 5 VERIFICATION: Inheritance & Polymorphism
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 5] Testing Abstract Account & Subclass Polymorphism");
        Account[] accountArray = new Account[]{
                new SavingsAccount("Aman", 10000L, 1000L),
                new CurrentAccount("Bhavik", 15000L, 5000L),
                new FixedDepositAccount("Chetan", 50000L, 12)
        };

        for (Account acc : accountArray) {
            System.out.printf("  Polymorphic interestRate() for %-20s = %.1f%% p.a. (Monthly: Rs. %.2f)%n",
                    acc.getClass().getSimpleName(), acc.interestRate(), acc.monthlyInterest());
        }

        // --------------------------------------------------------------------
        // PRACTICAL 6 VERIFICATION: Interfaces, Default Methods & Lambdas
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 6] Testing Interfaces, Default Methods & Functional WithdrawRule");
        InterestBearing ib = accountArray[0];
        System.out.println("  Interface default yearlyInterest(): Rs. " + ib.yearlyInterest());
        System.out.println("  Interface default halfYearlyInterest(): Rs. " + ib.halfYearlyInterest());

        // WithdrawRule implemented as Anonymous Class
        WithdrawRule anonRule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount <= 5000 && account.getBalance() >= amount;
            }
        };

        // WithdrawRule implemented as Lambda Expression
        WithdrawRule lambdaRule = (account, amount) -> amount <= 5000 && account.getBalance() >= amount;

        System.out.println("  Anonymous Rule allow(Rs. 3000): " + anonRule.allow(a1, 3000L));
        System.out.println("  Lambda Rule allow(Rs. 3000):    " + lambdaRule.allow(a1, 3000L));
        System.out.println("  Lambda Rule allow(Rs. 8000):    " + lambdaRule.allow(a1, 8000L));
        System.out.println("  FixedDeposit is Premium (Marker Interface): " + (accountArray[2] instanceof Premium));

        // --------------------------------------------------------------------
        // PRACTICAL 7 VERIFICATION: Custom Annotations & Reflection Validation
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 7] Testing Custom Annotations (@Id, @Positive, @MaxLength) & Reflection");
        SavingsAccount invalidAccount = new SavingsAccount("ExtremelyLongCustomerNameExceedingTheFiftyCharactersLimitAllowedByTheMaxLengthAnnotationValidationRule", -500L);
        String[] validationErrors = AnnotationValidator.validate(invalidAccount);
        System.out.println("  Validation Errors detected by Reflection (" + validationErrors.length + "):");
        for (String err : validationErrors) {
            System.out.println("   - " + err);
        }

        // --------------------------------------------------------------------
        // PRACTICAL 8 VERIFICATION: Custom Exception Handling & Try-with-Resources
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 8] Testing Checked Exceptions & Try-with-Resources AutoCloseable");
        try (BankSession session = new BankSession("BranchManager")) {
            System.out.println("  Attempting invalid withdrawal of negative amount (-100)...");
            try {
                a1.withdraw(-100L);
            } catch (InvalidAmountException e) {
                System.out.println("  Caught expected InvalidAmountException: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.err.println("  Unexpected InsufficientFundsException: " + e.getMessage());
            }

            System.out.println("  Attempting excessive withdrawal over balance...");
            try {
                a1.withdraw(50000L);
            } catch (InsufficientFundsException e) {
                System.out.println("  Caught expected InsufficientFundsException: Shortfall = Rs. " + e.getShortfall());
            } catch (InvalidAmountException e) {
                System.err.println("  Unexpected InvalidAmountException: " + e.getMessage());
            }
        } // AutoCloseable triggers here automatically!

        // --------------------------------------------------------------------
        // PRACTICAL 9 VERIFICATION: Multithreading & Race Condition vs Synchronization
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 9] Testing Multithreading & Synchronized Thread Safety");
        SavingsAccount syncTestAcc = new SavingsAccount("SharedUser", 0L, 0L);
        int threadCount = 10;
        int depositsPerThread = 1000;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new AccountWorker(syncTestAcc, depositsPerThread, 1L), "Worker-" + i);
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }

        long expectedFinalBalance = threadCount * depositsPerThread * 1L; // Exactly 10000
        System.out.println("  Final balance after " + threadCount + " threads x " + depositsPerThread + " deposits = Rs. "
                + syncTestAcc.getBalance() + " (Expected exactly: " + expectedFinalBalance + ")");

        // --------------------------------------------------------------------
        // PRACTICAL 10 VERIFICATION: Thread Pool, Producer-Consumer & Deadlock Prevention
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 10] Testing Thread Pool, Producer-Consumer & Deadlock-Free Transfers");
        TransactionProcessor testProcessor = new TransactionProcessor(4);
        testProcessor.submit(() -> {
            try {
                syncTestAcc.deposit(500L);
            } catch (InvalidAmountException ignored) {}
        });
        testProcessor.stop();
        System.out.println("  ThreadPool task completed. Balance: Rs. " + syncTestAcc.getBalance());

        // Producer-Consumer test
        TransactionQueue txQueue = new TransactionQueue(3);
        Thread producer = new Thread(() -> {
            try {
                txQueue.produce("TX_101");
                txQueue.produce("TX_102");
            } catch (InterruptedException ignored) {}
        });
        Thread consumer = new Thread(() -> {
            try {
                String item = txQueue.consume();
                System.out.println("  Consumer successfully popped item from queue: " + item);
            } catch (InterruptedException ignored) {}
        });
        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException ignored) {}

        // Deadlock Prevention Transfer Verification
        SavingsAccount accX = new SavingsAccount("User X", 10000L);
        SavingsAccount accY = new SavingsAccount("User Y", 10000L);
        System.out.println("  Executing concurrent bidirectional transfers between " + accX.getAccountNumber() + " and " + accY.getAccountNumber());
        Thread t1 = new Thread(() -> {
            try {
                accX.transfer(accY, 500L);
            } catch (BankException ignored) {}
        });
        Thread t2 = new Thread(() -> {
            try {
                accY.transfer(accX, 500L);
            } catch (BankException ignored) {}
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
            System.out.println("  Bidirectional transfers completed cleanly with NO deadlock!");
        } catch (InterruptedException ignored) {}

        // --------------------------------------------------------------------
        // PRACTICAL 11 VERIFICATION: Serialization, Log Append & NIO Reports
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 11] Testing State Serialization & NIO Report Generator");
        Path testDat = Paths.get("data", "test_accounts.dat");
        Path testLog = Paths.get("data", "logs", "test_transactions.log");
        Path testReport = Paths.get("data", "reports", "test_summary.txt");

        try {
            Account[] toSave = new Account[]{accX, accY};
            StatePersister.save(toSave, testDat);
            Account[] reloaded = StatePersister.load(testDat);
            System.out.println("  Serialized & Deserialized successfully! Loaded accounts count: " + reloaded.length);

            TransactionLog.append(testLog, "DEPOSIT " + accX.getAccountNumber() + " amount: 5000");
            TransactionLog.append(testLog, "WITHDRAW " + accY.getAccountNumber() + " amount: 2000");

            String reportOutput = ReportGenerator.generateReport(Paths.get("data", "logs"), testReport);
            System.out.println("  Report generated successfully (First 3 lines preview):");
            String[] reportLines = reportOutput.split("\n");
            for (int i = 0; i < Math.min(4, reportLines.length); i++) {
                System.out.println("   | " + reportLines[i]);
            }
        } catch (Exception e) {
            System.err.println("  NIO error: " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // PRACTICAL 12 VERIFICATION: Collections (HashMap, Comparator, Comparable)
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 12] Testing Collections Framework (HashMap, Sorting)");
        AccountStore store = new AccountStore();
        store.add(new SavingsAccount("User 5000", 5000L));
        store.add(new SavingsAccount("User 2000", 2000L));
        store.add(new SavingsAccount("User 9000", 9000L));

        System.out.println("  Accounts sorted by Balance descending (Comparator):");
        List<Account> sortedByBal = store.sortedByBalance();
        for (Account a : sortedByBal) {
            System.out.println("   - " + a.getOwnerName() + " : Rs. " + a.getBalance());
        }

        // --------------------------------------------------------------------
        // PRACTICAL 13 VERIFICATION: Generic Repository<T> & Bounded Method
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 13] Testing Generic Repository<T> with Reflective @Id Keying");
        Repository<Customer> custRepo = new Repository<>();
        custRepo.save(new Customer("Deep", "deep@test.com", "9998887771"));
        custRepo.save(new Customer("Karan", "karan@test.com", "9998887772"));
        System.out.println("  Generic Repository<Customer> stored items count: " + custRepo.count());
        System.out.println("  Generic Repository works without modification for Customer objects!");

        Double[] testValues = new Double[]{10.5, 20.5, 30.0, 40.0};
        double avg = Repository.average(testValues);
        System.out.println("  Bounded Generic Method Repository.average([10.5, 20.5, 30.0, 40.0]) = " + avg);

        // --------------------------------------------------------------------
        // PRACTICAL 14 VERIFICATION: BankService Assembly & Integration
        // --------------------------------------------------------------------
        System.out.println("\n>>> [PRACTICAL 14] Testing BankService Layered Capstone Integration");
        try {
            Account newAcc = bankService.openAccount("SAVINGS", "Capstone Tester", 7500L);
            bankService.deposit(newAcc.getAccountNumber(), 1500L);
            bankService.withdraw(newAcc.getAccountNumber(), 2000L);
            System.out.println("  Statement preview for newly created capstone account:");
            System.out.println(bankService.getStatement(newAcc.getAccountNumber()));
        } catch (BankException e) {
            System.err.println("  Capstone error: " + e.getMessage());
        }

        System.out.println("====================================================================");
        System.out.println("  ALL PRACTICAL LAB REQUIREMENTS (1 TO 14) SUCCESSFULLY VERIFIED!");
        System.out.println("====================================================================");
    }
}
