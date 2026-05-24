# portaltool

In Minecraft 1.16.1 Speedrunning, runners use this strategy known as 'preemptive' where you can use the pie chart to get a block entities spike on the pie chart when the subchunk containing the silverfish spawner from the portal room is rendered. Basically, this tool creates a 9x9 square grid of 16x16 'blocks' with the center square always being our start when we begin scanning from starter 5-way. Each square represents a chunk that the portal room has potential to be in.

When runners press F3+c, the position (player coordinates + looking angle) are copied to the clipboard which our program can then use to draw lines on the window with regards to the looking angle from the clipboard. When a captured line goes through a chunk (square on the grid), the square changes color to a yellow, and if 2 or more lines intersect a grid square, the darker it gets, like a heatmap.

