
# RollOrDie 

A simple minecraft plugin for version 1.21.10 using Paper.

Every few seconds it forces all the players on the server to roll a number between 1 and 20. The player with the smallest roll, as well as all the players that didn't roll get killed.

When killed by the game, the players do not lose experience points, but they still drop their items.

Made for small communities.

## Commands

This command changes the time between each phase of the rolling session.
```
/rod-settings <time_between_sessions|time_until_warn|time_after_warn> <seconds>
```
+ time_between_session - The base time between each rolling session (this value varies between 0.9 and 1.5 of itself)
+ time_until_warn - The time players have to roll before they receive a warning
+ time_after_warn - The remaining time to roll after receiving the warning

\
This command toggle the gambling sessions
```
/togglegamba
```






