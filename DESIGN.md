# 0.25 architecture

## Scheduling

Two alarms are scheduled:

1. `WINDOW` at `hard alarm - Smart Wake window`
2. `HARD` at the user's exact alarm time

The `HARD` alarm is intentionally NOT cancelled when Smart Wake fires early. It is only replaced after STOP/SNOOZE. This gives a crash-safe deadline fallback.

## Monitoring

`SmartWakeService` acquires a partial wakelock during the active Smart Wake window and records 30-second epochs.

`MotionCollector` calculates:

- RMS dynamic acceleration after removing gravity magnitude
- movement peaks >= 0.10 g

`HeartRateRepository` chooses:

1. recent Android `TYPE_HEART_RATE` event,
2. fresh Huami provider record,
3. no HR -> motion-only model.

## Decision

The engine does not call its output a sleep stage. It estimates wake readiness.

A single high score is insufficient. Smart Wake needs at least two of the last three epochs above the selected threshold and at least 90 seconds of monitoring.

## Fail-safe rules

- Exact hard alarm is always separately scheduled.
- Missing HR does not disable alarm logic.
- STOP schedules the next normal day.
- SNOOZE replaces the current hard/window intents with a one-off snooze alarm.
- WakeLock has a 45-minute safety timeout.
