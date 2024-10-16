# Orbus #

Four teams run around collecting orbs and racing to power their home bases before the others. Sabatoge the other teams to gain a competitive advantage. This was a small project I decided to take on in the summer of 2016.

### What makes this project cool? ###

1. Multiplayer! This was my first successful attempt at a multiplayer game, though I think it still might be pretty buggy. It's been a long time since I made this.
2. A decently functional AI! The AI teams in this game basically operate on a state machine; each bot can either be looking for orbs (and attacking on sight), defending the base, or attacking/sabatoging other bases. The game will balance the amounts of each, but it also skews the balance against some teams, simulating weakness. That is, a team might end up being highly defensive and not at all offensive. Or, vice versa.

### How do I try it? ###

Orbus is a Maven project. I do all this in IntelliJ.

1. Pull the repository.
2. Run `mvn package` to install the LWJGL natives.
3. Set VM option `Djava.library.path=target/natives`.
