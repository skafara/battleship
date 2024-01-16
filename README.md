# battleship
Battleship Game Server & Client

Seminar Work of KIV/UPS

> University of West Bohemia, Pilsen

Server: C++ (Linux)

Client: Java, JavaFX (Linux, Windows)

## Compilation

### Server

`/server$ cmake && cd build && make`

## Usage

### Server

`/server/build$ ./bserver --ip=<ip> --port=<port> --lim-clients=<lim-clients> --lim-rooms=<lim-rooms>`

	<ip>          - IP Address
	<port>        - Port
	<lim-clients> - Clients Count Limit
	<lim-rooms>   - Rooms Count Limit

### Client

`/client$ mvn javafx:run`

## Example

### Server

`/server/build$ ./bserver --ip=127.0.0.1 --port=50000 --lim-clients=20 --lim-rooms=10`

	Server is launched at 127.0.0.1:50000 (TCP) and is able to server 20 clients and 10 rooms.
