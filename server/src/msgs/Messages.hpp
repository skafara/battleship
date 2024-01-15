#pragma once

#include "Message.hpp"
#include "../game/Board.hpp"


namespace msgs {

	/// Messages Construction Util
	class Messages {
	public:
		/// Client
		enum class Client {
			kYou,
			kOpponent
		};

		static std::string Get_Client_Description(Client client) {
			if (client == Client::kYou) {
				return "YOU";
			} else {
				return "OPPONENT";
			}
		}

		/// Turn Result
		enum class Turn_Res {
			kHit,
			kMiss
		};

		static std::string Get_Turn_Res_Description(Turn_Res res) {
			if (res == Turn_Res::kHit) {
				return "HIT";
			} else {
				return "MISS";
			}
		}

		/// Duration
		enum class Duration {
			kShort,
			kLong
		};

		static std::string Get_Duration_Description(Duration duration) {
			if (duration == Duration::kShort) {
				return "SHORT";
			} else {
				return "LONG";
			}
		}

		/// State
		enum class State {
			kIn_Room,
			kIn_Game
		};

		static std::string Get_State_Description(State state) {
			if (state == State::kIn_Room) {
				return "ROOM";
			} else {
				return "GAME";
			}
		}

		/// Field
		enum class Field {
			kNone,
			kShip,
			kInvalidated
		};

		static std::string Get_Field_Description(Field field) {
			if (field == Field::kNone) {
				return "NONE";
			} else if (field == Field::kShip) {
				return "SHIP";
			} else {
				return "INVALIDATED";
			}
		}

		static Message Welcome();
		static Message Ack();
		static Message Keep_Alive();
		static Message Conn_Term();
		static Message Limit_Clients(size_t lim);
		static Message Nickname_Exists();
		static Message Room_Created(const std::string &code);
		static Message Limit_Rooms(size_t lim);
		static Message Room_Not_Exists();
		static Message Room_Full();
		static Message Board_Illegal();
		static Message Opponent_Nickname_Set(const std::string &nickname);
		static Message Opponent_Board_Ready();
		static Message Opponent_Room_Leave();
		static Message Game_Begin();
		static Message Turn_Set(Client client);
		static Message Turn_Result(size_t row, size_t col, Turn_Res res);
		static Message Opponent_Turn(size_t row, size_t col, Turn_Res res);
		static Message Turn_Not_You();
		static Message Turn_Illegal();
		static Message Game_End(Client client);
		static Message Opponent_No_Response(Duration duration);
		static Message Opponent_Rejoin();
		static Message Rejoin(State state, const std::string &code);
		static Message Board_State(Client client, const game::Board &board);
		static Message Invalidate_Field(Client client, size_t row, size_t col);
	};

} // msgs
