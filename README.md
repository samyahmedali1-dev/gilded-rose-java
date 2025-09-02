# Gilded Rose — Inventory Rules (Updated)

---

## Summary

The system updates each item's `SellIn` and `Quality` at the end of each day.  
Your task was to add support for **Conjured** items (they degrade faster) while keeping existing behavior for special items.

---

## Model

Each item has:
- `Name` (string) — e.g. `"Aged Brie"`, `"Sulfuras, Hand of Ragnaros"`, `"Backstage passes to a TAFKAL80ETC concert"`, `"Conjured Mana Cake"`, or any normal item.
- `SellIn` (int) — days remaining to sell the item.
- `Quality` (int) — value of the item.

> The `Item` class and `Items` property **must not be modified** (public contract).

---

## Rules (Authoritative)

### Universal invariants
- `0 <= Quality <= 50` for **all** items **except** `Sulfuras`.
- `Sulfuras` is legendary: `Quality == 80` and it **never changes** (neither `SellIn` nor `Quality`).

### Normal items
- Each day: `Quality -= 1`.
- After sell-by (`SellIn < 0`): `Quality -= 2`.
- Quality never drops below `0`.

### Aged Brie
- `Quality` **increases** over time; still capped at `50`.

### Backstage passes
- `SellIn > 10` → `Quality += 1`
- `6 <= SellIn <= 10` → `Quality += 2`
- `1 <= SellIn <= 5` → `Quality += 3`
- After concert (`SellIn < 0`) → `Quality = 0`
- Cap at `50` before concert.

### Sulfuras
- Legendary. No changes to `SellIn` or `Quality` (`Quality = 80`).

### Conjured (NEW)
- Conjured items degrade **twice as fast** as non-conjured equivalents:
  - Before sell date: degrade by `2` per day (instead of `1`).
  - After sell date: degrade by `4` per day (instead of `2`).
- Identification: item `Name` contains `"Conjured"` (or switch to a type flag later).

---

## One-day examples

| Before (Name, SellIn, Quality) | After one day |
|---|---|
| Normal, 10, 20 | 9, 19 |
| Normal, 0, 20  | -1, 18 |
| Aged Brie, 2, 0 | 1, 1 |
| Backstage, 15, 20 | 14, 21 |
| Backstage, 10, 25 | 9, 27 |
| Backstage, 5, 48 | 4, 50 (cap) |
| Backstage, 0, 20 | -1, 0 |
| Sulfuras, 0, 80 | 0, 80 |
| Conjured, 3, 6 | 2, 4 |
| Conjured, 0, 6 | -1, 2 |

---

# Design Patterns & Fuzzy Matching — Implementation Notes

---

## Overview
We refactored `UpdateQuality` into a small, testable architecture using **Strategy**, **Factory**, and a **Decorator** style base class, supported by small utilities (clamping, logging, normalization). This makes behavior extensible, readable, and easy to unit-test, while a conservative fuzzy-matching layer preserves backward compatibility with name-based item inputs.

---

## Applied Patterns (what & why)

### 1. Strategy Pattern — per-item update behaviour
**What:** `ItemUpdateStrategy` interface / abstract base and concrete strategies:
- `NormalStrategy`
- `AgedBrieStrategy`
- `BackstageStrategy`
- `SulfurasStrategy`
- `ConjuredStrategy`

**Why:** each item has distinct rules. Strategy isolates each rule set into its own class so adding a new item type only requires a new strategy — no invasive change to a monolithic method.

**Responsibilities:** implement `update(Item item)` to mutate `SellIn` and `Quality` for that item type.

**Benefit:** readability, single responsibility, easy unit testing.

---

### 2. Factory Pattern — choose the correct strategy
**What:** `ItemStrategyFactory` resolves an `ItemUpdateStrategy` for a given `Item`.

**Why:** centralizes the mapping from item identity → behaviour. The factory handles normalization, exact lookup, fuzzy matching fallback, and returns a strategy the system calls.

**Behaviour:** exact matches first (fast), then fuzzy matching only when necessary.

---
### 3. Decorator Pattern — add cross-cutting behaviour to policies

**What:** Concrete **decorators** implement  **wrap** a delegate `UpdatePolicy` instance.

**Why:** Keeps domain policies (e.g. `DegradingPolicy`, `BackstagePolicy`) focused on business rules while allowing orthogonal features to be composed without modifying core logic. This preserves single responsibility, improves testability, and enables runtime composition/configuration of behaviour.


---



### 4. Small utilities
- **QualityClamp** — single place for `0..50` (except Sulfuras).
- **NameNormalizer** — lowercase, trim, collapse whitespace, remove punctuation/diacritics if needed.
- **FuzzyMatcher** — encapsulates Levenshtein / token matching logic.
---

## Fuzzy Matching — rationale & implementation

> **Context:** The original kata identifies item types by `name`. To tolerate typos and variations (e.g., `"Conjured Mana Cake"`, `"conjurd mana cake"`), we added a conservative fuzzy matching stage.

### Pipeline (factory)
1. **Normalize** the name (`trim`, `lowercase`, remove punctuation).
2. **Exact match** against canonical names → return strategy immediately.
3. **Substring tokens** (fast): detect `"conjured"`, `"backstage"`, `"aged brie"`, `"sulfuras"`.
4. **Fuzzy match** if no exact/substr match:
   - Score against known tokens using Levenshtein (or trigram similarity).
   - Apply thresholds (see below).
5. **Disambiguate**:
   - Prefer exact > substring > fuzzy.
   - If multiple fuzzy candidates, select highest score and apply precedence (Sulfuras > Backstage > AgedBrie > Conjured > Normal).
6. **Fallback** to `NormalStrategy`.




## How we applied SOLID principles — rationale & implementation


### S — Single Responsibility Principle (SRP)
1. **Idea:** each class has one reason to change.
2. **What we did:**
   - Policy classes implement single business rules (`DegradingPolicy`, `BackstagePolicy`, `AgedBriePolicy`, `SulfurasPolicy`).
   - `ItemStrategyFactory` only resolves & composes policies.


### O — Open/Closed Principle (OCP)

**Idea:** open for extension, closed for modification.

**What we did:**
- Add behavior by creating new `UpdatePolicy` implementations or decorators; existing classes remain unchanged.
- `ItemStrategyFactory` wires new strategies via registration/config (factory registration or DI), not by editing existing policy internals.

**Result:** add `ConjuredPolicy` by creating a class and wiring it — no edits to existing code.

---

### L — Liskov Substitution Principle (LSP)

---

### I — Interface Segregation Principle (ISP)

---

### D — Dependency Inversion Principle (DIP)

**Idea:** depend on abstractions, not concretes.

**What we did:**
- All composition is done against the `UpdatePolicy` abstraction; factories and decorators accept dependencies via constructor injection.
- Tests can replace concrete implementations with mocks or fakes easily.

