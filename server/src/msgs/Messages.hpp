#pragma once

#include "Message.hpp"


namespace msgs {

	class Messages {
	public:
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

		static Message Welcome();
		static Message Ping();
		static Message Ack();
		static Message Conn_Term();
		static Message Limit_Clients(size_t lim);
		static Message Nickname_Prompt();
		static Message Nickname_Exists();
		static Message Room_Created(const std::string &code);
		static Message Limit_Rooms(size_t lim);
		static Message Room_Not_Exists();
		static Message Room_Full();
		static Message Board_Illegal();
		static Message Opponent_Nickname_Set(const std::string &nickname);
		static Message Opponent_Board_Ready();
		static Message Game_Begin();
		static Message Turn_Set(Client client);
		static Message Turn_Result(Turn_Res res);
		static Message Turn_Not_You();
		static Message Turn_Illegal();
		static Message Game_End(Client client);
	};

} // msgs
