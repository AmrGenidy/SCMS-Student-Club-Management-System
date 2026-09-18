# Student Club Management System (SCMS)

A JavaFX desktop application to streamline the administration of university student organizations. The system automates member registration, event organisation and financial tracking, providing a common platform for Club Administrators (Presidents) and general Members.

**Course:** CS320 Software Engineering, Ozyegin University (Spring 2026)
**Team:** Mohamed Amr Ibrahim Genedi, Hamza Yüksel, Emir, Bora

## Project documentation

All project deliverables are in [`/docs`](docs):

- [Software Development Plan (SDP) — v1.5](docs/SDPv1.5.pdf)
- [Software Requirements Specification (SRS) — v1.5](docs/SRSv1.5.pdf)
- [Software Detailed Design (SDD) — v1.3](docs/SDDv1.3.pdf)
- [Software Test Plan (STP) — v1.6](docs/STPv1.6.pdf)

Each document includes a revision history showing the contribution of each team member.

## Architecture

The application follows a strict 3-Tier layered architecture:

- **Presentation Layer** (`scms.presentation`) — JavaFX controllers and FXML views. Handles user interaction and display.
- **Application Layer** (`scms.application`) — business logic. Validates input, enforces business rules (event quotas, budget calculations) and manages workflow.
- **Data Layer** (`scms.data`) — Data Access Objects (DAOs) and SQL connection management using prepared statements.

Design patterns applied: **Singleton** (database connection), **Observer** (UI updates on data changes), **State** (event registration behaviour), **MVC** (UI layer), **DAO** (data access).

See the [SDD](docs/SDDv1.3.pdf) for the full architectural description, class diagrams and sequence diagrams.

## Tech stack

- Java (JDK 17+)
- JavaFX 17.0.2 for the GUI
- MySQL (via `mysql-connector-j` 8.0.33)
- JUnit 5 for unit testing
- Maven for build management

## Repository layout

```
/
├── docs/          Project documentation (SDP, SRS, SDD, STP)
├── src/           Java source code
│   └── main/java/scms/
│       ├── application/    Business logic layer
│       ├── data/           Data access layer
│       └── presentation/   UI layer
└── README.md
```
