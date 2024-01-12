#pragma once

#include <string>
#include <sstream>
#include <map>
#include <vector>
#include <exception>

#include "../game/Board.hpp"


namespace msgs {

	class IllegalMessageException : public std::runtime_error {
	public:
		explicit IllegalMessageException(const std::string &text) : std::runtime_error(text) {
			//
		}
	};

	template <typename T>
	concept IntegralValue = std::is_integral_v<std::remove_reference_t<T>>;

	enum class MessageType {
		kWelcome,
		kKeep_Alive,
		kAck,
		kLimit_Clients,
		kConn_Term,
		kNickname_Prompt,
		kNickname_Set,
		kNickname_Exists,
		kRoom_Create,
		kRoom_Join,
		kRoom_Created,
		kLimit_Rooms,
		kRoom_Not_Exists,
		kRoom_Full,
		kRoom_Leave,
		kBoard_Ready,
		kBoard_Illegal,
		kOpponent_Nickname_Set,
		kOpponent_Board_Ready,
		kOpponent_Room_Leave,
		kGame_Begin,
		kTurn_Set,
		kTurn,
		kTurn_Result,
		kOpponent_Turn,
		kTurn_Not_You,
		kTurn_Illegal,
		kGame_End,
		kOpponent_No_Response,
		kOpponent_Rejoin,
		kRejoin,
		kBoard_State
	};

	static const std::map<MessageType, const std::string> kMessageType_String {
		{MessageType::kWelcome, "WELCOME"},
		{MessageType::kKeep_Alive, "KEEP_ALIVE"},
		{MessageType::kAck, "ACK"},
		{MessageType::kLimit_Clients, "LIMIT_CLIENTS"},
		{MessageType::kConn_Term, "CONN_TERM"},
		{MessageType::kNickname_Prompt, "NICKNAME_PROMPT"},
		{MessageType::kNickname_Set, "NICKNAME_SET"},
		{MessageType::kNickname_Exists, "NICKNAME_EXISTS"},
		{MessageType::kRoom_Create, "ROOM_CREATE"},
		{MessageType::kRoom_Join, "ROOM_JOIN"},
		{MessageType::kRoom_Created, "ROOM_CREATED"},
		{MessageType::kLimit_Rooms, "LIMIT_ROOMS"},
		{MessageType::kRoom_Not_Exists, "ROOM_NOT_EXISTS"},
		{MessageType::kRoom_Full, "ROOM_FULL"},
		{MessageType::kRoom_Leave, "ROOM_LEAVE"},
		{MessageType::kBoard_Ready, "BOARD_READY"},
		{MessageType::kBoard_Illegal, "BOARD_ILLEGAL"},
		{MessageType::kOpponent_Nickname_Set, "OPPONENT_NICKNAME_SET"},
		{MessageType::kOpponent_Board_Ready, "OPPONENT_BOARD_READY"},
		{MessageType::kOpponent_Room_Leave, "OPPONENT_ROOM_LEAVE"},
		{MessageType::kGame_Begin, "GAME_BEGIN"},
		{MessageType::kTurn_Set, "TURN_SET"},
		{MessageType::kTurn, "TURN"},
		{MessageType::kTurn_Result, "TURN_RESULT"},
		{MessageType::kOpponent_Turn, "OPPONENT_TURN"},
		{MessageType::kTurn_Not_You, "TURN_NOT_YOU"},
		{MessageType::kTurn_Illegal, "TURN_ILLEGAL"},
		{MessageType::kGame_End, "GAME_END"},
		{MessageType::kOpponent_No_Response, "OPPONENT_NO_RESPONSE"},
		{MessageType::kOpponent_Rejoin, "OPPONENT_REJOIN"},
		{MessageType::kRejoin, "REJOIN"},
		{MessageType::kBoard_State, "BOARD_STATE"},
	};

	static const std::map<MessageType, const size_t> kMessageType_Params_Cnt{
		{MessageType::kKeep_Alive, 0},
		{MessageType::kAck, 0},
		{MessageType::kNickname_Set, 1},
		{MessageType::kRoom_Create, 0},
		{MessageType::kRoom_Join, 1},
		{MessageType::kRoom_Leave, 0},
		{MessageType::kBoard_Ready, game::Board::kShip_Fields_Cnt},
		{MessageType::kTurn, 1}
	};

	class Message {
	public:
		template<typename... Args>
		Message(const MessageType type, Args &&... args) {
			_type = type;
			Store_Params(std::forward<Args>(args)...);
		};

		MessageType Get_Type() const;
		const std::string &Get_Param(size_t idx) const;

		void Store_Param(const std::string &param) {
			_params.push_back(param);
		};

		std::string Serialize() const;
		static Message Deserialize(const std::string &pair);

	private:
		static constexpr char kEscape_Char = '\\';
		static constexpr char kParam_Delimiter = '|';

		MessageType _type;
		std::vector<std::string> _params;

		template<typename T, typename ...Args>
		void Store_Params(T &&param, Args &&...params) {
			Store_Param(param);
			Store_Params(std::forward<Args &&>(params)...);
		};;
		void Store_Params() const {};

		template<IntegralValue T>
		void Store_Param(T &&param) {
			const std::string str = std::to_string(param);
			_params.push_back(str);
		};
	};

} // msgs
