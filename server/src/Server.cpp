#include "Server.hpp"

#include "msgs/Communicator.hpp"
#include "msgs/Messages.hpp"
#include "game/StateMachine.hpp"
#include "game/Room.hpp"

#include <thread>


Server::Server(const std::string &addr, uint16_t port, size_t lim_clients, size_t lim_rooms) :
		_lim_clients(lim_clients), _lim_rooms(lim_rooms), _sock_acceptor(addr, port) {
	//
}
#include "iostream"
void Server::Serve() {
	for (;;) {
		/*std::vector<std::string> board = {
				"..XX...XXX",
				"X.........",
				"X.....X..X",
				"X.....X...",
				"X.........",
				".....X...X",
				"..........",
				"X.....X..X",
				"X.....X...",
				"......X..."
		};
		game::Board b{};
		for (int row = 0; row < 10; ++row) {
			for (int col = 0; col < 10; ++col) {
				if (board[row][col] == 'X') {
					b.Set_Ship(row, col);
				}
			}
		}

		std::cout << b.Is_Valid() << std::endl;*/

		std::unique_ptr<ntwrk::Socket> sock = Accept_Connection();

		std::lock_guard lck{Get_Mutex()};

		msgs::Communicator::Send(*sock, msgs::Messages::Welcome());

		if (_clients.size() >= _lim_clients) {
			Refuse_Connection(std::move(sock));
			continue;
		}

		std::shared_ptr<game::Client> client = std::make_shared<game::Client>(std::move(sock));
		_clients.push_back(client);

		std::thread thread{&Server::Serve_Client, this, client};
		thread.detach();
	}
}

std::unique_ptr<ntwrk::Socket> Server::Accept_Connection() const {
	return std::make_unique<ntwrk::Socket>(_sock_acceptor.Accept());
}

void Server::Refuse_Connection(std::unique_ptr<ntwrk::Socket> sock) const {
	msgs::Communicator msgc{*sock};

	msgc.Send(msgs::Messages::Limit_Clients(_lim_clients));
	msgc.Send(msgs::Messages::Conn_Term());
}
#include "game/Board.hpp"
#include "iostream"
void Server::Serve_Client(std::shared_ptr<game::Client> client) {
	game::StateMachine::Run(*this, client);

	//client->Send_Msg(msgs::Messages::Nickname_Prompt());
	//client->Await_Ack();

	//game::StateMachine state_machine{*this, client};
	//state_machine.Run();

	/*client->Await_Msg({msgs::MessageType::kNickname_Set});
	client->Send_Msg(msgs::Messages::Ack());

	client->Await_Msg({msgs::MessageType::kRoom_Create, msgs::MessageType::kRoom_Join});
	client->Send_Msg(msgs::Messages::Room_Created("1234"));

	client->Await_Msg({msgs::MessageType::kRoom_Leave, msgs::MessageType::kBoard_Ready});
	client->Send_Msg(msgs::Messages::Ack());

	client->Send_Msg(msgs::Messages::Opponent_Nickname_Set("abcd"));
	client->Await_Ack();
	client->Send_Msg(msgs::Messages::Opponent_Board_Ready());
	client->Await_Ack();
	client->Send_Msg(msgs::Messages::Game_Begin());
	client->Await_Ack();

	Terminate_Client(client);*/
}

void Server::Terminate_Client(std::shared_ptr<game::Client> client) {
	client->Send_Msg(msgs::Messages::Conn_Term());
	_clients.erase(std::ranges::find(_clients, client));
}

size_t Server::Get_Lim_Rooms() const {
	return _lim_rooms;
}

bool Server::Is_Exceeded_Lim_Rooms() const {
	return _rooms.size() >= _lim_rooms;
}

const std::string &Server::Create_Room() {
	for (;;) {
		const std::string code = game::Room::Generate_Code();
		if (Exists_Room(code)) {
			continue;
		}

		std::shared_ptr<game::Room> room = std::make_shared<game::Room>(code);
		_rooms.push_back(room);
		return room->Get_Code();
	}
}

bool Server::Exists_Room(const std::string &code) const {
	const auto it = std::ranges::find(_rooms, code, &game::Room::Get_Code);
	return it != std::end(_rooms);
}

std::shared_ptr<game::Room> Server::Get_Room(const std::string &code) const {
	const auto it = std::ranges::find(_rooms, code, &game::Room::Get_Code);
	return *it;
}

std::shared_ptr<game::Room> Server::Get_Room(const std::shared_ptr<game::Client> client) const {
	return _rooms[0]; // TODO
}

bool Server::Is_Nickname_Active(const std::string &nickname) const {
	const auto it = std::ranges::find(_clients, nickname, &game::Client::Get_Nickname);
	return it != std::end(_clients);
}

bool Server::Is_Nickname_Disconnected(const std::string &nickname) const {
	return _disconnected.contains(nickname);
}

void Server::Disconnect_Client(const std::shared_ptr<game::Client> client, game::State state) {
	_disconnected.insert(std::make_pair(client->Get_Nickname(), std::make_pair(state, Get_Room(client))));
}

void Server::Reconnect_Client(std::shared_ptr<game::Client> client) const {

}

std::mutex &Server::Get_Mutex() const {
	return _mutex;
}
