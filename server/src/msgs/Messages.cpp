#include "Messages.hpp"


namespace msgs {

	Message Messages::Welcome() {
		return {MessageType::kWelcome,
			"bserver - Battleship server",
			"Seminar Work of KIV/UPS, 2024",
			"Stanislav Kafara, skafara@students.zcu.cz",
			"University of West Bohemia, Pilsen"
		};
	}

	Message Messages::Ping() {
		return {MessageType::kPing};
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

	Message Messages::Nickname_Prompt() {
		return {MessageType::kNickname_Prompt};
	}

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

	Message Messages::Game_Begin() {
		return {MessageType::kGame_Begin};
	}

	Message Messages::Turn_Set(Messages::Client client) {
		return {MessageType::kTurn_Set, Get_Client_Description(client)};
	}

	Message Messages::Turn_Result(Messages::Turn_Res res) {
		return {MessageType::kTurn_Result, Get_Turn_Res_Description(res)};
	}

	Message Messages::Turn_Not_You() {
		return {MessageType::kTurn_Not_You};
	}

	Message Messages::Turn_Illegal() {
		return {MessageType::kTurn_Illegal};
	}

	Message Messages::Game_End(Messages::Client client) {
		return {MessageType::kGame_End, Get_Client_Description(client)};
	}

} // msgs