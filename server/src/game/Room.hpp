#pragma once

#include "Client.hpp"
#include "Board.hpp"


namespace game {

	/// Room
	class Room {
	public:
		/// Clients Count
		static constexpr size_t kClients_Cnt = 2;
		/// Room Code Length
		static constexpr size_t kRoom_Code_Len = 4;

		/// Transparently constructs
		/// \param code Room code
		explicit Room(const std::string &code);

		/// Generates a random room code
		/// \return Random code
		static std::string Generate_Code();
		/// Returns room code
		/// \return Code
		const std::string &Get_Code() const;

		/// Returns clients in the room
		/// \return Clients in the room
		const std::array<std::shared_ptr<Client>, kClients_Cnt> &Get_Clients() const;
		/// Returns whether a room is full
		/// \return Bool
		bool Is_Full();
		/// Joins a client
		/// \param client Client
		void Join(const std::shared_ptr<Client> client);

		/// Returns the client's opponent
		/// \param client Client
		/// \return Opponent
		Client &Get_Opponent(const Client &client);
		/// Returns the client's board
		/// \param client Client
		/// \return Board
		Board &Get_Board(const Client &client);
		/// Sets the client's board
		/// \param client Client
		/// \param board Board
		void Set_Board(const Client &client, const Board &board);

		/// Resets boards
		void Reset_Boards();

		/// Sets client's board status ready
		/// \param client Client
		void Set_Board_Ready(const Client &client);
		/// Gets client's board ready status
		/// \param client Client
		/// \return Bool
		bool Is_Board_Ready(const Client &client);

		/// Sets random client on turn
		void Set_Random_Client_On_Turn();
		/// Sets client's opponent on turn
		/// \param client Client
		void Set_Opponent_On_Turn(const Client &client);

		/// Returns client on turn
		/// \return Client on turn
		Client &Get_Client_On_Turn() const;
		/// Returns whether the client is on turn
		/// \param client Client
		/// \return Bool
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
