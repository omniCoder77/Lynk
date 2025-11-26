The most common cause of broken anchors in Markdown is placing emojis at the **start** of a header. Different parsers (GitHub, VS Code, IntelliJ) handle the slug generation for emojis differently (some strip them, some encode them).

To fix this while keeping the "beautiful" aesthetic, I have moved the emojis to the **end** of the headers. This guarantees that the generated link ID will always be standard text (e.g., `#code-of-conduct`), ensuring the Table of Contents works everywhere.

Here is the fixed **CONTRIBUTING.md**:

***

# Contributing to Lynk

First off, thank you for considering contributing to Lynk! It's people like you that make the open-source community such an amazing place to learn, inspire, and create.

We welcome contributions of all forms: bug fixes, new features, documentation improvements, and performance optimizations. This document guides you through the process of contributing to maintain the high quality and performance standards of the platform.

## 📋 Table of Contents

1.  [Code of Conduct](#code-of-conduct)
2.  [Getting Started](#getting-started)
3.  [Reporting Issues](#reporting-issues)
4.  [Development Workflow](#development-workflow)
5.  [Coding Standards](#coding-standards)
6.  [Commit Guidelines](#commit-guidelines)
7.  [Pull Request Process](#pull-request-process)

---
<a id="code-of-conduct"></a>
## Code of Conduct 🤝

By participating in this project, you are expected to uphold our Code of Conduct. We expect all contributors to treat one another with respect and professionalism. Harassment or abusive behavior will not be tolerated.

---
<a id="getting-started"></a>
## Getting Started 🚀

1.  **Fork the repository** on GitHub.
2.  **Clone your fork** locally:
    ```bash
    git clone https://github.com/YOUR-USERNAME/Lynk.git
    cd Lynk
    ```
3.  **Set up the environment** following the instructions in the [README.md](README.md). Ensure you have Docker running and certificates generated.
4.  **Create a branch** for your specific contribution.

---
<a id="reporting-issues"></a>
## Reporting Issues 🐛

### Bug Reports
We use GitHub Issues to track bugs. Report a bug by opening a new issue; it's that easy!
**Great Bug Reports tend to have:**
- A quick summary and/or background.
- Steps to reproduce (be specific!).
- What you expected would happen vs. what actually happened.
- Logs, screenshots, or infrastructure details (e.g., "Kafka container crashed with error X").

### Feature Requests
We love new ideas. If you want to suggest a feature, please open an issue and tag it as `enhancement`. Describe the use case and why it fits into the Lynk architecture.

---
<a id="development-workflow"></a>
## Development Workflow 🛠

We follow a standard Feature Branch workflow.

1.  **Sync your fork:** Keep your `main` branch up to date with the original repository.
2.  **Create a Branch:**
    ```bash
    git checkout -b feature/my-amazing-feature
    # or
    git checkout -b fix/memory-leak-issue
    ```
3.  **Code:** Implement your changes.
4.  **Test:** Run the specific service tests and ensure the docker environment is stable.

---
<a id="coding-standards"></a>
## Coding Standards 💻

Since Lynk is a high-performance reactive platform, we have strict guidelines to ensure stability.

### 1. Kotlin & Style
*   Follow the **[official Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)**.
*   Code should be concise but readable. Prefer expression bodies for simple functions.
*   **Immutability:** Prefer `val` over `var` and immutable data structures wherever possible.

### 2. Reactive Principles (Crucial)
This project uses **Spring WebFlux (Project Reactor)**.
*   🚫 **NEVER** block the main thread. Avoid `Thread.sleep()`, blocking JDBC calls, or `.block()`/`.blockFirst()` in production code.
*   ✅ **ALWAYS** use non-blocking operators (`flatMap`, `map`, `zip`, `filter`).
*   If you must wrap a blocking call (e.g., a legacy library), offload it to a bounded elastic scheduler:
    ```kotlin
    Mono.fromCallable { blockingCall() }
        .subscribeOn(Schedulers.boundedElastic())
    ```

### 3. Testing
*   **Unit Tests:** Required for all business logic. Use **JUnit 5** and **Mockk**.
*   **Integration Tests:** We use **Testcontainers** for verifying interactions with Postgres, Cassandra, and Kafka. Do not mock the database in integration tests; spin up the container.

---
<a id="commit-guidelines"></a>
## Commit Guidelines 📝

We follow the **[Conventional Commits](https://www.conventionalcommits.org/)** specification. This allows us to automatically generate changelogs.

**Format:** `<type>(<scope>): <subject>`

**Types:**
*   `feat`: A new feature
*   `fix`: A bug fix
*   `docs`: Documentation only changes
*   `style`: Changes that do not affect the meaning of the code (white-space, formatting, etc)
*   `refactor`: A code change that neither fixes a bug nor adds a feature
*   `perf`: A code change that improves performance
*   `test`: Adding missing tests or correcting existing tests
*   `chore`: Changes to the build process or auxiliary tools (e.g., Dockerfile, CI/CD)

**Examples:**
*   `feat(auth): implement TOTP verification logic`
*   `fix(messaging): resolve race condition in room creation`
*   `chore(infra): update kafka docker image version`

---
<a id="pull-request-process"></a>
## Pull Request Process 📥

1.  **Self-Review:** Look through your code one last time. Did you leave any `TODO`s or `print` statements?
2.  **Update Documentation:** If you changed an API or environment variable, update the `README.md` or Swagger docs.
3.  **Run Tests:** Ensure `./gradlew test` passes locally.
4.  **Open PR:**
    *   Target the `main` branch.
    *   Reference any relevant issues (e.g., "Closes #42").
    *   Provide a clear description of *what* you changed and *why*.
5.  **Review:** Maintainers will review your code. Be open to feedback!

---

**Happy Coding!** 🚀