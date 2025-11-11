# Elevator Saga – Coding Challenge 🛗
**Challenge:** Optimize elevator control logic for the [Elevator Saga](https://play.elevatorsaga.com/) game.  
**Author / Source:** [Magnus Wolffelt and contributors](https://play.elevatorsaga.com/) (version 1.6.5) ([play.elevatorsaga.com](https://play.elevatorsaga.com/))  

---

## Table of Contents
- [Overview](#overview)
- [Objective](#objective)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation / Setup](#installation-setup)
- [How To Run](#how-to-run)
- [API / Integration](#api-integration)
- [Scoring & Metrics](#scoring-metrics)
- [Strategy & Approach](#strategy-approach)
- [Submission Guidelines](#submission-guidelines)
- [License](#license)

---

## Overview
Elevator Saga is a browser‑based game where you program the behaviour of one or more elevators in a simulated building. The goal is to efficiently transport passengers by controlling elevator movement, scheduling stops, and managing state. ([play.elevatorsaga.com](https://play.elevatorsaga.com/))

---

## Objective
- Implement elevator logic that minimises waiting time and maximises throughput.
- Achieve better metrics than the baseline sample code.
- Possibly tackle harder “scenarios” (e.g., more floors, more elevators, higher passenger load).
- Write clean, maintainable code with comments and (optional) tests.

---

## Getting Started

### Prerequisites
- A modern web browser with JavaScript enabled.
- (Optional) A local development environment if you clone the source code.

### Installation / Setup
1. Visit [Elevator Saga](https://play.elevatorsaga.com/) in your browser.
2. If you wish to work locally:
   ```bash
   git clone https://github.com/MagnusWolffelt/ElevatorSaga.git
   cd ElevatorSaga
   # open the index.html in your browser
   ```
   (Note: Adjust repository URL if using your own fork.)
3. Open the “Help & API documentation” link on the site to familiarise with available callbacks & APIs.

---

## How To Run
- In the game interface:
  1. Click “Apply” to apply your code.
  2. Click “Run tests” to execute scenarios.
- Locally:
  - Open your page and navigate through scenario list.
  - Use browser dev tools (console, breakpoints) to debug.
- Save your best code for submission or sharing.

---

## API / Integration
From the in‑game documentation, you’ll typically interact with:
- `init(elevators, floors)`: Called once at start; you set up event handlers here.
- Elevator‑ and floor‑objects, each of which allow you to bind event handlers such as `on("idle")`, `on("floor_button_pressed")`, `on("up_button_pressed")`, etc.
- Methods such as `goToFloor(floorNum)`, `loadFactor()`, `destinationQueue`, etc.
Use these to drive elevator logic (when to move, pick up, drop off).

Please consult the “Help & API documentation” page on the game for full details.

---

## Scoring & Metrics
Key performance indicators (displayed in the interface):
- **Transported**: Number of passengers served.
- **Transported/s**: Throughput — how many per second.
- **Avg waiting time**: Average time passengers waited.
- **Max waiting time**: The worst‑case wait.
- **Moves**: Number of elevator movements.
Goal: minimise average & max waiting times, maximise throughput, and do so with fewer unnecessary moves.

---

## Strategy & Approach
Here are tips / ideas for tackling the challenge:
- Use **requests queue** at each floor for up/down, and maintain an **elevator state machine** (idle, moving, loading, unloading).
- Prioritise floors with waiting passengers.
- Minimise **empty runs** (elevators travelling without passengers).
- Balance load across multiple elevators (if available).
- Employ heuristics: e.g., when elevator is idle, split up/down load; when loading, fill until loadFactor threshold; when going up, pick up up‑requests along the way.
- Consider “zoning” — certain elevators serve certain floor ranges.
- Keep logic simple but robust; avoid edge‑cases like “passengers waiting forever”.
- Profile your results (via metrics) and iterate.

---

## Submission Guidelines
- Provide your source code file (e.g., `elevator.js`).
- Include a short README (this file) describing your approach.
- Optionally include test scenario results/screen‑shots showing your metrics.
- (If relevant) Fork the original repo, commit your changes, and push to your own GitHub; include link.
- Ensure code is commented and easy to follow.

---

## License
Include appropriate license (for your submission) — e.g., MIT License — and mention that the original Elevator Saga code belongs to Magnus Wolffelt and contributors.

---

Thank you for taking on the Elevator Saga challenge — good luck, and may your elevators run smooth and swift! 🚀

---

*Last updated: 2025‑11‑11*
