# SCMS — Student Club Management System

A 3-package JavaFX desktop application built for the CS320 Software Engineering course at Özyeğin University.

See `IMPLEMENTATION_PLAN.md` for a detailed breakdown of every defect found in the original codebase and how it was fixed.

---

## Architecture

```
scms.presentation  → JavaFX FXML views + controllers
scms.application   → Business logic (Manager classes), entities, AccessControl, PasswordUtil
scms.data          → DAO classes + DatabaseConnection singleton
```

The presentation layer must never import from `scms.data` directly — `ArchitectureTest` enforces this.

---

## Prerequisites

- JDK 17 (`maven.compiler.release=17` is set in `pom.xml`)
- Maven 3.6+
- PostgreSQL 14+ running locally

---

## Database setup

```bash
# Create the database
createdb scms

# Apply the schema (creates tables + seeds demo users + 1000 TL initial budget)
psql -d scms -f src/main/resources/schema.sql
```

Connection parameters default to `localhost:5432`, user `postgres`, password `postgres`. Override them at launch time with JVM system properties:

```bash
mvn javafx:run \
  -Dscms.db.url=jdbc:postgresql://localhost:5432/scms \
  -Dscms.db.user=postgres \
  -Dscms.db.password=postgres
```

---

## Running

```bash
mvn javafx:run
```

Demo accounts (created by `schema.sql`):

| Role  | Student ID | Password   |
|-------|-----------|------------|
| Admin | 12345678  | admin123   |
| Member| 87654321  | member123  |

---

## Tests

```bash
mvn test
```

Test classes map to the STPv2 test cases as follows:

| STP Test ID | Test class |
|---|---|
| T-SRS-SCMS-001 | `MemberManagerTest` |
| T-SRS-SCMS-002 | `EventManagerTest` |
| T-SRS-SCMS-003 | `FinanceManagerTest` |
| T-SRS-SCMS-004 | `SessionManagerTest` + `AccessControlTest` |
| T-SRS-SCMS-005 | `MemberManagerTest` |
| T-SRS-SCMS-006 | `EventManagerTest` |
| T-SRS-SCMS-NF-01 | Manual smoke test (run the app) |
| T-SRS-SCMS-NF-02 | `ArchitectureTest` |
| T-SRS-SCMS-NF-03 | `SqlInjectionTest` + `PasswordUtilTest` |

---

## Key fixes vs. the initial codebase

1. **Password authentication** — login now requires Student ID + password (SHA-256 hashed).
2. **No more role-spoofing** — the role combobox was removed from the login screen; the role is read from the database row.
3. **Role-based UI** — Members CRUD, Finance and the admin Dashboard tab are hidden for members. `AccessControl` enforces the same rules from the application layer.
4. **Profile view** — new `ProfileView.fxml` shows the logged-in member's info and upcoming events.
5. **"Today" event accepted** — `EventManager` now compares `LocalDate`s instead of full timestamps.
6. **Per-member event sign-ups** — new `event_signups` table prevents duplicate sign-ups and gives an audit trail.
7. **3-package architecture enforced** — `ArchitectureTest` will fail the build if a presentation file imports `scms.data` or vice-versa.
8. **Configurable DB connection** — system properties `scms.db.url|user|password` override the hardcoded defaults.
