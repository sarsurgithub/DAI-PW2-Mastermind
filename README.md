# DAI-PW2-Mastermind
Client/Server application to play games of Mastermind

# Definition of the protocol

## Section 1 - overview

The mastermind protocol is meant to play a game of mastermind versus a server over the network
The mastermind protocol is a client-server protocol
The client connects to a server to start a game of mastermind, the server then sends a message containing the starting point of the game. The client then tries to find the solution and sends it to the servers, which answers either with a message signfying the win of the client or with a clue for the answer. This goes on until the client found the answer or request the game to stop or restart.

## Section 2 - Transport protocol

The mastermind protocol uses the TCP protocol. The server runs on port 44444.
The client has to know the IP adress of the server to connect to it. It establishes the connection with the server.
The server closes the connection when the game is finished or if it is requested by the client.

## Section 3 - Messages

The client can send the following messages:
- `START`: used to start a game
- `RULES`: used to get the rules of the game
- `HELP`: used to get the accepted requests from the server
- `TRY <proposition>` : used to propose an answer
	- `<proposition>`: the solution that the client wants to try, as a string of 4 chars accepting only certain 	symbols
- `RESTART`: used to start a new game
- `QUIT`: requests the server to close the connection

The server can send the following messages:
- `OK`: used to notify the client that the connection was successful and the server is ready to receive commands
- `GAMES STARTED` : used to notfiy the client that the server will now accept TRY requests
- `SEND RULES`: sends the rules of the game
- `COMMANDS`: sends the accepted commands from the server
- `ANSWER <clues>` : used to send the answer with clues to a try back to the client
	- `<clues>` : string of 4 chars accepting the symbols meaning correct color or correct color and placement, and a char 	meaning "nothing"
- `FINISHED <status>` : used to answer the client when he has won the game or has no more tries
	- `<status>` can either be WON or LOST
- `ERROR <code>` : used to notify the client that an error occured
	- `418` : the TRY proposition was malformed -> request another try but I'm a mastermind game not tic tac toe
  - `403`: using TRY outside of a game, does not close the connection
  - `400`: every other malformation closes the connection

## Section 4 examples

![Sequence diagram of the protocol](./images/diagramSeqProt.png)