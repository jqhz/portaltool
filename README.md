# portaltool

## Abstract

In Minecraft Java 1.16.1 Speedrunning, runners use this strategy known as 'preemptive' where you can use the pie chart to get a block entities spike on the pie chart when the subchunk containing the silverfish spawner from the portal room is rendered. Basically, this tool creates a 9x9 square grid of 16x16 'blocks' with the center square always being our start when we begin scanning from starter 5-way. Each square represents a chunk that the portal room has potential to be in.

When runners press F3+C, the position (player coordinates + looking angle) are copied to the clipboard which our program can then use to draw lines on the window with regards to the looking angle from the clipboard. When a captured line goes through a chunk (square on the grid), the square changes color to a yellow, and if 2 or more lines intersect a grid square, the darker it gets, like a heatmap. Since F3 angle use a wrapped system (-180 to 180) and the f3 + c coords use an unwrapped system (-infinity to +infinity). We can use the formula ((angle + 360) mod 360) to convert F3+C looking angle to the angle from F3 to use to draw the lines on our grid.

-0°/0° - South (+Z)

90° - West (-X) 

-180°/180° - North (-Z)

-90° - East (+X)

Grid is South-based 0° oriented meaning that the top of the grid is South, right is West, bottom is North, and left is East.

## Usage

1. Runner enters the stronghold and starts scanning in the starter 5-way.
2. Runner presses F3+C at the instance they get their spike.
3. Runner can press F3+C again at a more an alternative looking angle to triangulate the position of the portal room.
4. System automatically detects the clipboard for a F3+C command and draws the line on the grid.
5. After the runner has successfully completed the run, the runner presses the restart button to clear the grid for the next run.

## References/Inspiration

[Ninjabrain bot](https://github.com/Ninjabrain1/Ninjabrain-Bot) - Understand how to capture the F3+C input from clipboard to abstract the looking angle

## Tech Stack

- Java
- Java Swing
- Gradle for dependencies
