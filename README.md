# ⚡ High-Performance Order Matching Engine

An ultra-fast, thread-safe financial matching engine written in Java. This core engine implements strict **Price-Time Priority** (FIFO) and is designed for high-concurrency environments, utilizing event-driven architecture to achieve low-latency trade execution.

## 🧠 Core Architecture & Concepts

This project is built around the fundamental principles of modern exchange architecture (similar to mechanisms used by global equities and crypto exchanges).

* **Price-Time Priority:** Orders are matched first by the best available price (Price Priority). If multiple orders share the exact same price, they are executed in the exact order they arrived (Time Priority).
* **Maker-Taker Model:** Incoming orders (Takers) sweep the book to consume resting liquidity. If an order is not fully filled, the remaining quantity rests in the order book (becoming a Maker).
* **Asynchronous Persistence:** The matching engine operates entirely in-memory. Trades are published to an event queue and persisted by background Virtual Threads, ensuring the core matching algorithm never blocks on database I/O.

---

## 🏗️ System Components

### 1. Domain Model (`com.exchange.model`)
* **`Order`**: Represents a trader's intent to Buy or Sell. Utilizes `public final` fields for immutable financial contracts, and an `AtomicLong` for thread-safe tracking of the `remaining` quantity during partial fills.
* **`Trade`**: The immutable, historical record of a matched transaction. Stamped with `System.nanoTime()` for strict deterministic sequencing.

### 2. The Order Book (`com.exchange.orderbook`)
* **`PriceLevel`**: A custom wrapper around an `ArrayDeque`. It acts as a blazing-fast, O(1) FIFO waiting line for all orders at a specific price point.
* **`OrderBook`**: The data structure housing all active orders. 
    * Uses a `TreeMap` (Red-Black Tree) to maintain strictly sorted price levels (O(log n) lookups).
    * The **Bid** (Buy) book uses `Comparator.reverseOrder()` to ensure the highest bidders are at the top.
    * The **Ask** (Sell) book naturally sorts to keep the lowest sellers at the top.
    * Protected by a `ReentrantLock` to prevent data corruption during highly concurrent sweeps.

### 3. The Engine (`com.exchange.engine`)
* **`MatchingEngine`**: The brain of the system. It locks the order book, processes incoming Taker orders against resting Maker orders, calculates partial/full fills using `Math.min()`, and generates `Trade` objects.

### 4. Event-Driven Persistence (`com.exchange.persistence`)
* **`TradeEventQueue`**: A `LinkedBlockingQueue` implementing the Producer-Consumer pattern. 
* **`TradePersistenceWorker`**: A background worker powered by **Java 21 Virtual Threads**. It sleeps with zero CPU usage until a trade occurs, wakes up instantly to process the event, and ensures the matching engine is never slowed down by downstream systems.

---

## 🔄 The Trade Lifecycle

1.  A new `Order` enters the `MatchingEngine`.
2.  The engine acquires the `ReentrantLock` for the specific `OrderBook`.
3.  The engine checks the opposing side of the book (e.g., Buy order checks the Sell book).
4.  **Match Found:** If prices overlap (crossing the spread), a `Trade` is generated and quantities are atomically deducted.
5.  **Partial Fill:** If the incoming order is larger than the resting order, it consumes the resting order, pops it from the `PriceLevel` queue, and continues sweeping the next best price.
6.  **Resting:** If the incoming order is exhausted, or no overlapping prices remain, any leftover quantity is placed into a `PriceLevel` queue to rest as a Maker.
7.  The lock is released.
8.  Generated `Trade` events are pushed to the non-blocking `TradeEventQueue`.

---

## 🧪 Concurrency Testing

The engine includes a rigorous multi-threaded stress test (`MatchingEngineConcurrencyTest`). 

Using a `FixedThreadPool` of 50 simultaneous threads, it fires thousands of perfectly overlapping Buy and Sell orders at the exact same millisecond. This mathematically proves the thread-safety of the `ReentrantLock` and the `AtomicLong` state variables, ensuring zero race conditions or dropped orders under heavy load.

**To run the test suite:**
`mvn test`

---

## 🚀 Future Enhancements (Roadmap)
While the core matching algorithm is fully functional, a production-grade exchange requires additional layers:
* **Advanced Order Types:** Implementing Immediate-Or-Cancel (IOC) and Fill-Or-Kill (FOK) order logic to bypass the resting Order Book.
* **Broker Gateway API:** Building a REST/WebSocket API layer with an `OrderRouter` to securely accept JSON payloads from registered Brokers and route them to symbol-specific matching engines (e.g., AAPL, TSLA).
* **Multi-Asset Scaling:** Scaling the single `OrderBook` into a `ConcurrentHashMap<String, MatchingEngine>` to process hundreds of different ticker symbols in parallel.