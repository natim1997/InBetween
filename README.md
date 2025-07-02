# InBetween

**InBetween** is a personal and shared scheduling Android app that helps you manage tasks, goals, and collaborate with friends.

## Table of Contents

1. [Features](#features)
2. [Usage](#usage)
3. [Permissions & Sharing](#permissions--sharing)
4. [Target Audience](#target-audience)
5. [Technology Stack](#technology-stack)
6. [License](#license)

---

## Features

* **Weekly Calendar View**: See a scrollable week of dates, select any day to view your schedule.
* **Task Management**:

  * Create, edit, and delete single or recurring tasks (daily/weekly).
  * Set custom recurrence end dates and exclude specific dates.
* **Goal Planning**:

  * Define multi-session goals (e.g., study 3 times/week).
  * Auto-generate suggested time slots for goal sessions, with manual override.
* **Real-Time Sync**: All data stored in Firebase Firestore with live updates.
* **Friend Collaboration**:

  * Send requests to friends by email.
  * Choose **View-only** or **Full-access** permission.
  * Accept or decline incoming requests.
  * View or modify friends’ calendars based on permission.
* **Dark Mode Support**: UI adapts to system theme.

## Usage

1. **Sign Up / Login**: Create an account with email & password.
2. **Home Screen**:

   * Navigate weeks using arrows.
   * Tap **+** to add a task or goal.
3. **Adding Tasks**:

   * Enter title, date, start & end time.
   * Choose daily/weekly recurrence & optional end/exclusions.
4. **Adding Goals**:

   * Define title, sessions per week, session duration, start & end dates.
   * Review suggested slots → Accept, Reject, or Customize each.
5. **Contact List**:

   * Send friend requests by email.
   * Manage incoming requests (Accept/Decline).
   * View friends under **View-only** or **Full-access** tabs.
   * Tap a friend to view their calendar.
   * **My Schedule** button returns to your own calendar.

## Permissions & Sharing

* **Owner**: Full control over own tasks/goals.
* **View-only**: See friend’s schedule; cannot add/edit/delete.
* **Full-access**: Add or delete tasks on friend’s calendar.
* Firestore security rules enforce these permissions.

## Target Audience

* Individuals looking to organize daily or weekly tasks.
* Students planning study sessions across the week.
* Teams or groups who need shared access to each other’s schedules.
* Anyone who wants goal-based session planning with minimal manual setup.

## Technology Stack

* **Language**: Kotlin
* **UI Framework**: Android View system + Material Components
* **Backend**: Firebase Authentication & Cloud Firestore
* **Date/Time API**: java.time (LocalDate, LocalTime)


## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
