# Roadmap

Features planned for Deciary but not built yet.

## Decision types: where is your gut reliable?

**Idea:** tag each letter with the kind of decision it is. Once enough letters are opened, show which kinds of decision your gut calls well and which it doesn't.

**Why:** today Deciary tells you how your decisions turned out one letter at a time. Grouping them by type turns "how often am I right?" into "where can I trust my gut, and where should I slow down?". That's the real lesson the app is there to teach.

**How it could work:**

- **Tagging:** an optional type picker on the first writing step, next to the question. It's one tap, and skipping it is fine. Possible types: Money, Career, Relationships, Family, Health, Home, Learning, Other.
- **The insight:** for each type with enough opened letters, combine what the app already records:
  - **Verdicts:** how often it was Earned it, Bad luck, Lucky break or Lesson.
  - **Calibration:** how sure you were compared with how often your prediction came true. For example, "Money: you're usually more confident than you should be."
  - A plain summary per type, such as "Career: your calls held up in 4 of 5 letters."
- **Where it shows:** possibly a view of its own reached from the shelf, with a short note on Home when a pattern appears.

**To decide before building:**

- The final type list, and whether people can add their own.
- How many opened letters a type needs before it shows a result (so a single letter doesn't count as a pattern).
- Whether existing letters can be tagged afterwards.
- Where the insight lives in the app.

**Privacy:** tags are stored inside the encrypted letters file like everything else, and they're included in backups. Nothing leaves the phone.
