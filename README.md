# __How to Run our project__

## __Instalations needed:__ <i>(our versions)</i>
- JavaFX (e.g. javafx-sdk-20.0.1)
- Java (e.g. SDK 19)
- Gradle (e.g. 7.6.1)

## __How to run it:__
### <b>First way to do it:</b>
- <i>gradlew build</i>
- <i>gradlew runServer</i> - to start the Server (must be the first thing to do)
- <i>gradlew runUser</i> - to start a User (to start a single User)
<br>

### <b>Second way to do it:</b>
- <i>script</i> - to run a script.bat file that will start the server and ask how many Users you want to start. This script will open a <i>cmd prompt</i> to the Server and to every User and will start running the User Application.<br>

<b>ATTENTION:</b> The applications might be overlaped.

## __Application JavaFX:__
Very simple to use. 
- First you need to login with a valid User or register a new one and then login with that one.
- Choose the Game Mode you want to play, Simple or Ranked.
- The User will be putted on a queue until the game has enoughs Users to start.
- After the Game starts you may press the <b><i>ATTACK</i></b> button.
- When a Game ends, the GameOver screen is presented and it is possible to play another Game, Logout and exit the Aplication.<br>

<b>Login Examples:</b>
- Username: andre; Password: 12345;
- Username: diogo; Password: 12345;
- Username: ricardo; Password: 12345;

## __Features Implemented:__
- Login and Register 
    - Empty User not allowed
    - Tokens for validation
- Simple Mode and Ranked Mode 
    - 8 different ranks
- Queues for different Modes
    - Possible to leave the Queue while the Game hasn't started
- Concurrency
    - No race conditions for adding or removing Users to Queues
- Thread pools
    - UserHandlers 
    - Matchmaking Queues
- Fault Tolerance for 20 seconds
    - After 20sec the User that is On wins the Game
    - After 20sec if both Users are Off no one wins

# Demo

https://github.com/diogocosta876/Concurrent-Server-Client-Game/assets/24635445/06f807ec-4480-4431-a972-de3f9ec78274

