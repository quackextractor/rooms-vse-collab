Rooms
=====

A small text adventure. Explore an old castle, fight what lives in it, and
reach the village of Villedor.

Running
-------

Windows:   double-click play.cmd
Anywhere:  java -jar rooms.jar

Run it from this folder, or the game will not find its map. Java 17 or newer
is required: https://adoptium.net

Files
-----

rooms.jar   the game
rooms.csv   the level, read from the folder the game is started in
map.png     the same level, drawn
play.cmd    Windows launcher

Goal
----

Reach the gate to Villedor. It opens only for strength 30 or more. Some
other doors need a stat too; the game says which when you try them.

Items and fights
----------------

Picking up an item adds its stats, discarding it takes them away again.
Weapons add their damage to your strength when you attack.

An enemy blocks the room until you fight it or back away. Whoever has more
agility strikes first; on a tie the enemy does. Wounds last only for the
fight: win or lose, your health is restored afterwards. Lose and you are
pushed back to the previous room, and the enemy that beat you recovers too.

Saving
------

1 starts a new game, 2 continues the saved one. The game saves to
savegame.dat in this folder after every move. Winning deletes it.

Controls
--------

N, E, S, W   move north, east, south or west
L            look around
I            inventory and stats
P            pick up an item
D            discard an item
M            map of discovered rooms
U            undo the last move, pick-up or discard (not past a fight)
H            help
Q            save and quit
Y, N         fight or back away when an enemy blocks the room

Documentation (Czech):
https://docs.google.com/document/d/1T-GgtVTeWU5gGcM30NfqiNvnhVv8UQBvjJlyul5rVAU/edit?usp=sharing
