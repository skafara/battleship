#pragma once

#include "Client.hpp"
#include "Board.hpp"


namespace game {

	class Room {
	public:
		// mutex lock?
		Room(const std::string &code);

		static std::string Generate_Code();
		const std::string &Get_Code() const;

		const std::array<std::shared_ptr<Client>, 2> &Get_Clients() const;
		bool Is_Full();
		void Join(const std::shared_ptr<Client> client);

		//bool Has_Opponent(const Client &client);
		Client &Get_Opponent(const Client &client);
		Board &Get_Board(const Client &client);
		void Set_Board(const Client &client, const Board &board);

		void Set_Board_Ready(const Client &client);
		bool Is_Board_Ready(const Client &client);
		//bool Is_Both_Boards_Ready() const;

		void Set_Random_Client_On_Turn();
		void Set_Opponent_On_Turn(const Client &client);

		Client &Get_Client_On_Turn() const;
		bool Is_On_Turn(const Client &client) const;

	private:
		std::string _code;

		std::array<std::shared_ptr<Client>, 2> _clients;
		std::array<Board, 2> _boards;

		std::array<bool, 2> _boards_ready;

		size_t _client_on_turn_idx;

		size_t Get_Client_Idx(const Client &client) const;
	};

} // game
