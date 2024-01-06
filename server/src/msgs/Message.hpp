#pragma once

#include <string>
#include <sstream>
#include <map>


namespace msgs {

	template <typename T>
	concept IntegralValue = std::is_integral_v<std::remove_reference_t<T>>;

	enum class MessageType {
		kWelcome,
		kPing,
		kPong,
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
		kGame_Begin,
		kTurn_Set,
		kTurn,
		kTurn_Result,
		kTurn_Not_You,
		kTurn_Illegal,
		kGame_End
	};

	static const std::map<MessageType, const std::string> kMessageType_String {
		{MessageType::kWelcome, "WELCOME"},
		{MessageType::kPing, "PING"},
		{MessageType::kPong, "PONG"},
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
		{MessageType::kGame_Begin, "GAME_BEGIN"},
		{MessageType::kTurn_Set, "TURN_SET"},
		{MessageType::kTurn, "TURN"},
		{MessageType::kTurn_Result, "TURN_RESULT"},
		{MessageType::kTurn_Not_You, "TURN_NOT_YOU"},
		{MessageType::kTurn_Illegal, "TURN_ILLEGAL"},
		{MessageType::kGame_End, "GAME_END"},
	};

	static const std::map<MessageType, const size_t> kMessageType_Params_Cnt{
		{MessageType::kPong, 0},
		{MessageType::kAck, 0},
		{MessageType::kNickname_Set, 1},
		{MessageType::kRoom_Create, 0},
		{MessageType::kRoom_Join, 1},
		{MessageType::kRoom_Leave, 0},
//		{MessageType::kBoard_Ready, 20},
		{MessageType::kBoard_Ready, 2},
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

		std::string Serialize() const;
		static Message Deserialize(const std::string &pair);

	private:
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
		void Store_Param(const std::string &param) {
			_params.push_back(param);
		};
	};

} // msgs
