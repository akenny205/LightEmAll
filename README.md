# LightEmAll
LightEmAll is a recreation of a classic game style. The player is presented with a grid of tiles, and a central powersource. The goal is to rotate the tiles in such a way that the light reaches every single tile. 

### Setup
This recreation is customizable in the size of the game. When creating an instance of the class LightEmAll, simply input the width and a height you desire. Each game will be random, as long as you input a new Random() object as the third input in the constructor. Download the src file, and make sure to set up the run configuration with the project name, and "Main class" as "tester.Main". Click run with the correct configuration, and the game should pop up in a new window.

### Info
LightEmUp was made using minimum spanning trees and kruskals algorithm. Each game is unique. HINT: As the board creation is through a MST, the implementation of the MST makes it so their are no valid tiles facing the outside of the board. 
Also, the MST only makes 1 valid tree, so the game is quite challenging. I reccomend starting with a 5 x 5 or 6 x 6.


<img width="243" alt="Screenshot 2025-01-27 at 8 44 05 PM" src="https://github.com/user-attachments/assets/b760331c-d82d-471f-8c70-c0fcf5e1eedd" />
