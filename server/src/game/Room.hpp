#pragma once

#include "Client.hpp"
#include "Board.hpp"


namespace game {

	class Room {
	public:
		static constexpr size_t kClients_Cnt = 2;
		static constexpr size_t kRoom_Code_Len = 4;

		explicit Room(const std::string &code);

		static std::string Generate_Code();
		const std::string &Get_Code() const;

		const std::array<std::shared_ptr<Client>, kClients_Cnt> &Get_Clients() const;
		bool Is_Full();
		void Join(const std::shared_ptr<Client> client);

		Client &Get_Opponent(const Client &client);
		Board &Get_Board(const Client &client);
		void Set_Board(const Client &client, const Board &board);

		void Reset_Boards();

		void Set_Board_Ready(const Client &client);
		bool Is_Board_Ready(const Client &client);

		void Set_Random_Client_On_Turn();
		void Set_Opponent_On_Turn(const Client &client);

		Client &Get_Client_On_Turn() const;
		bool Is_On_Turn(const Client &client) const;

	private:
		std::string _code;

		std::array<std::shared_ptr<Client>, kClients_Cnt> _clients;
		std::array<Board, kClients_Cnt> _boards;

		std::array<bool, kClients_Cnt> _boards_ready;

		size_t _client_on_turn_idx;

		size_t Get_Client_Idx(const Client &client) const;
	};

} // game
