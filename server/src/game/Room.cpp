#include "Room.hpp"
#include "../util/Generator.hpp"

#include <algorithm>


namespace game {

	Room::Room(const std::string &code) : _code(code) {
		//
	}

	std::string Room::Generate_Code() {
		std::ostringstream osstream;
		for (size_t i = 0; i < 4; ++i) {
			osstream << (util::Generator::From_Range(0, 9));
		}
		return osstream.str();
	}

	const std::string &Room::Get_Code() const {
		return _code;
	}

	const std::array<std::shared_ptr<Client>, 2> &Room::Get_Clients() const {
		return _clients;
	}

	bool Room::Is_Full() {
		return !std::ranges::any_of(_clients, [](const std::shared_ptr<Client> &client) {return client == nullptr;});
	}

	void Room::Join(const std::shared_ptr<Client> client) {
		for (size_t i = 0; i < 2; ++i) {
			if (_clients[i] == nullptr) {
				_clients[i] = client;
				return;
			}
		}

		throw std::runtime_error{"Joining Full Room"};
	}

	Client &Room::Get_Opponent(const Client &client) {
		return *(_clients[(Get_Client_Idx(client) + 1) % 2]);
	}

	Board &Room::Get_Board(const Client &client) {
		return _boards[Get_Client_Idx(client)];
	}

	void Room::Set_Board_Ready(const Client &client) {
		_boards_ready[Get_Client_Idx(client)] = true;
	}

	bool Room::Is_Board_Ready(const Client &client) {
		return _boards_ready[Get_Client_Idx(client)];
	}

	/*bool Room::Is_Both_Boards_Ready() const {
		return std::ranges::all_of(_boards_ready, [](bool ready) {return ready;});
	}*/

	void Room::Set_Random_Client_On_Turn() {
		_client_on_turn_idx = util::Generator::From_Range(0, 1);
	}

	void Room::Set_Opponent_On_Turn(const Client &client) {
		_client_on_turn_idx = Get_Client_Idx(Get_Opponent(client));
	}

	Client &Room::Get_Client_On_Turn() const {
		return *(_clients[_client_on_turn_idx]);
	}

	bool Room::Is_On_Turn(const Client &client) const {
		return Get_Client_On_Turn() == client;
	}

	size_t Room::Get_Client_Idx(const Client &client) const {
		for (size_t i = 0; i < _clients.size(); ++i) {
			if (*(_clients[i]) == client) {
				return i;
			}
		}

		throw std::runtime_error{"Client Not In Room"};
	}

	void Room::Set_Board(const Client &client, const Board &board) {
		_boards[Get_Client_Idx(client)] = board;
	}

	void Room::Reset_Boards() {
		for (size_t i = 0; i < _clients.size(); ++i) {
			_boards[i] = Board{};
			_boards_ready[i] = false;
		}
	}

} // game