#include "Messages.hpp"

#include "../game/Board.hpp"


namespace msgs {

	Message Messages::Welcome() {
		return {MessageType::kWelcome,
			"bserver - Battleship server",
			"Seminar Work of KIV/UPS, 2024",
			"Stanislav Kafara, skafara@students.zcu.cz",
			"University of West Bohemia, Pilsen"
		};
	}

	Message Messages::Ack() {
		return {MessageType::kAck};
	}

	Message Messages::Conn_Term() {
		return {MessageType::kConn_Term};
	}

	Message Messages::Limit_Clients(size_t lim) {
		return {MessageType::kLimit_Clients, lim};
	}

	/*Message Messages::Nickname_Prompt() {
		return {MessageType::kNickname_Prompt};
	}*/

	Message Messages::Nickname_Exists() {
		return {MessageType::kNickname_Exists};
	}

	Message Messages::Room_Created(const std::string &code) {
		return {MessageType::kRoom_Created, code};
	}

	Message Messages::Limit_Rooms(size_t lim) {
		return {MessageType::kLimit_Rooms, lim};
	}

	Message Messages::Room_Not_Exists() {
		return {MessageType::kRoom_Not_Exists};
	}

	Message Messages::Room_Full() {
		return {MessageType::kRoom_Full};
	}

	Message Messages::Board_Illegal() {
		return {MessageType::kBoard_Illegal};
	}

	Message Messages::Opponent_Nickname_Set(const std::string &nickname) {
		return {MessageType::kOpponent_Nickname_Set, nickname};
	}

	Message Messages::Opponent_Board_Ready() {
		return {MessageType::kOpponent_Board_Ready};
	}

	Message Messages::Opponent_Room_Leave() {
		return {MessageType::kOpponent_Room_Leave};
	}

	Message Messages::Game_Begin() {
		return {MessageType::kGame_Begin};
	}

	Message Messages::Turn_Set(Client client) {
		return {MessageType::kTurn_Set, Get_Client_Description(client)};
	}

	Message Messages::Turn_Result(size_t row, size_t col, Turn_Res res) {
		return {MessageType::kTurn_Result, game::Board::Serialize_Field(row, col), Get_Turn_Res_Description(res)};
	}

	Message Messages::Opponent_Turn(size_t row, size_t col, Turn_Res res) {
		return {MessageType::kOpponent_Turn, game::Board::Serialize_Field(row, col), Get_Turn_Res_Description(res)};
	}

	Message Messages::Turn_Not_You() {
		return {MessageType::kTurn_Not_You};
	}

	Message Messages::Turn_Illegal() {
		return {MessageType::kTurn_Illegal};
	}

	Message Messages::Game_End(Client client) {
		return {MessageType::kGame_End, Get_Client_Description(client)};
	}

	Message Messages::Opponent_No_Response(Messages::Duration duration) {
		return {MessageType::kOpponent_No_Response, Get_Duration_Description(duration)};
	}

	Message Messages::Opponent_Rejoin() {
		return {MessageType::kOpponent_Rejoin};
	}

	Message Messages::Rejoin(State state, const std::string &code) {
		return {MessageType::kRejoin, Get_State_Description(state), code};
	}

	Message Messages::Board_State(Client client, const game::Board &board) {
		Message msg{MessageType::kBoard_State, Get_Client_Description(client)};
		for (size_t row = 0; row < game::Board::kSize; row++) {
			for (size_t col = 0; col < game::Board::kSize; col++) {
				if (board.Is_Ship(row, col)) {
					if (board.Is_Guess(row, col)) {
						msg.Store_Param(Get_Turn_Res_Description(Turn_Res::kHit));
					} else {
						if (client == Client::kYou) {
							msg.Store_Param(Get_Field_Description(Field::kShip));
						}
						else {
							msg.Store_Param(Get_Field_Description(Field::kNone));
						}
					}
				} else {
					if (board.Is_Guess(row, col)) {
						msg.Store_Param(Get_Turn_Res_Description(Turn_Res::kMiss));
					} else {
						msg.Store_Param(Get_Field_Description(Field::kNone));
					}
				}
			}
		}
		return msg;
	}

} // msgs