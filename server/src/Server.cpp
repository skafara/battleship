#include "Server.hpp"

#include "msgs/Communicator.hpp"
#include "msgs/Messages.hpp"
#include "game/StateMachine.hpp"
#include "game/Room.hpp"

#include <thread>
#include <algorithm>


Server::Server(const std::string &addr, uint16_t port, size_t lim_clients, size_t lim_rooms) :
		_lim_clients(lim_clients), _lim_rooms(lim_rooms), _sock_acceptor(addr, port) {
	//
}

#include "iostream"
void Server::Serve() {
	std::thread th_clients_terminator{&Server::Clients_Terminator, this};

	for (;;) {
		std::unique_ptr<ntwrk::Socket> sock = Accept_Connection();

		std::lock_guard lck{Get_Mutex()};
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
	msgs::Communicator::Send(*sock, msgs::Messages::Limit_Clients(_lim_clients));
	msgs::Communicator::Send(*sock, msgs::Messages::Conn_Term());
}

void Server::Serve_Client(std::shared_ptr<game::Client> client) {
	client->Send_Msg(msgs::Messages::Welcome());
	client->Set_Last_Active(std::chrono::steady_clock::now());
	game::StateMachine::Run(*this, client);
}

void Server::Terminate_Client(std::shared_ptr<game::Client> client) {

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
	return *(std::ranges::find(_rooms, code, &game::Room::Get_Code));
}

std::shared_ptr<game::Room> Server::Get_Room(const std::shared_ptr<game::Client> client) const {
	for (const std::shared_ptr<game::Room> room : _rooms) {
		const auto &f = room->Get_Clients();
		if (std::ranges::find(f, client) != std::end(f)) {
			return room;
		}
	}

	return nullptr;
}

void Server::Destroy_Room(const std::shared_ptr<game::Room> room) {
	_rooms.erase(std::ranges::find(_rooms, room));
}

void Server::Erase_Disconnected_Client(const std::string &nickname) {
	_disconnected.erase(std::ranges::find(_disconnected, nickname, &game::Client::Get_Nickname));
}

bool Server::Is_Nickname_Active(const std::string &nickname) const {
	const auto it = std::ranges::find(_clients, nickname, &game::Client::Get_Nickname);
	return it != std::end(_clients);
}

bool Server::Is_Nickname_Disconnected(const std::string &nickname) const {
	const auto it = std::ranges::find(_disconnected, nickname, &game::Client::Get_Nickname);
	return it != std::end(_disconnected);
}

void Server::Disconnect_Client(const std::shared_ptr<game::Client> client) {
	const game::State &state = client->Get_State();
	if (state == game::State::kInit || state == game::State::kIn_Lobby) {
		client->Send_Msg(msgs::Messages::Conn_Term());
		_clients.erase(std::ranges::find(_clients, client));
		return;
	}

	_disconnected.push_back(client);
	_clients.erase(std::ranges::find(_clients, client));

	std::shared_ptr<game::Room> room = Get_Room(client);
	if (room && room->Is_Full()) {
		const game::Client &opponent = room->Get_Opponent(*client);
		opponent.Send_Msg(msgs::Messages::Opponent_No_Response(msgs::Messages::Duration::kShort));
	}

	client->Send_Msg(msgs::Messages::Conn_Term());
	client->Close_Socket();
}

void Server::Reconnect_Client(std::shared_ptr<game::Client> &client) {
	const auto it = std::ranges::find(_disconnected, client->Get_Nickname(), &game::Client::Get_Nickname);
	const std::shared_ptr<game::Client> disconnected = *it;
	_disconnected.erase(it);

	std::unique_ptr<ntwrk::Socket> sock = client->Give_Up_Socket();
	auto last_active = client->Get_Last_Active();

	*std::ranges::find(_clients, client) = disconnected;
	client = disconnected;

	client->Replace_Socket(std::move(sock));
	client->Set_Last_Active(last_active);

	const std::shared_ptr<game::Room> room = Get_Room(client);

	if (client->Get_State() == game::State::kIn_Room) {
		client->Send_Msg(msgs::Messages::Rejoin(msgs::Messages::State::kIn_Room, room->Get_Code()));
	}
	else {
		client->Send_Msg(msgs::Messages::Rejoin(msgs::Messages::State::kIn_Game, room->Get_Code()));
	}

	if (room->Is_Board_Ready(*client)) {
		client->Send_Msg(msgs::Messages::Board_State(msgs::Messages::Client::kYou, room->Get_Board(*client)));
	}

	if (room->Is_Full()) {
		const game::Client &opponent = room->Get_Opponent(*client);
		opponent.Send_Msg(msgs::Messages::Opponent_Rejoin());

		client->Send_Msg(msgs::Messages::Opponent_Nickname_Set(opponent.Get_Nickname()));
		if (room->Is_Board_Ready(opponent)) {
			client->Send_Msg(msgs::Messages::Opponent_Board_Ready());
		}

		if (room->Is_Board_Ready(*client)) {
			opponent.Send_Msg(msgs::Messages::Board_State(msgs::Messages::Client::kOpponent, room->Get_Board(*client)));
		}
		if (room->Is_Board_Ready(opponent)) {
			client->Send_Msg(msgs::Messages::Board_State(msgs::Messages::Client::kOpponent, room->Get_Board(opponent)));
		}
	}

	if (client->Get_State() == game::State::kIn_Game) {
		if (room->Is_On_Turn(*client)) {
			client->Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kYou));
		}
		else {
			client->Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kOpponent));
		}

		if (room->Is_Full()) {
			if (room->Is_On_Turn(*client)) {
				room->Get_Opponent(*client).Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kOpponent));
			}
			else {
				room->Get_Opponent(*client).Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kYou));
			}
		}
	}
}

std::mutex &Server::Get_Mutex() const {
	return _mutex;
}

void Server::Clients_Terminator() {
	for (;;) {
		std::unique_lock lck{Get_Mutex()};
		if (_disconnected.empty()) {
			lck.unlock();
			std::this_thread::sleep_for(Timeout_Long);
			continue;
		}

		const auto now = std::chrono::steady_clock::now();
		auto min_no_terminate_time_point = _disconnected[0]->Get_Last_Active();
		for (auto it = _disconnected.begin(); it != _disconnected.end(); ) {
			const std::shared_ptr<game::Client> client = *it;
			const auto duration = std::chrono::duration_cast<std::chrono::seconds>(now - client->Get_Last_Active());
			std::cout << duration.count() << std::endl;
			if (duration >= Timeout_Long) {
				std::shared_ptr<game::Room> room = Get_Room(client);
				if (room) {
					game::Client &opponent = room->Get_Opponent(*client);
					opponent.Send_Msg(msgs::Messages::Opponent_No_Response(msgs::Messages::Duration::kLong));
					opponent.Set_State(game::State::kIn_Lobby);
					Destroy_Room(room);
				}

				it = _disconnected.erase(it);
				continue;
			}

			if (client->Get_Last_Active() < min_no_terminate_time_point) {
				min_no_terminate_time_point = client->Get_Last_Active();
			}

			++it;
		}

		lck.unlock();
		std::this_thread::sleep_until(min_no_terminate_time_point + Timeout_Long);
	}
}
