#pragma once

#include <map>

#include "../msgs/Message.hpp"
#include "../I_ServerOps.hpp"


class I_ServerOps;

namespace game {

	class StateMachine;
	using t_Handler = std::function<bool (StateMachine &, const msgs::Message &)>;

	class StateMachine {
	public:
		static void Run(I_ServerOps &server, std::shared_ptr<Client> client);

	private:
		static const std::map<State, std::set<msgs::MessageType>> kExpected_Msgs;
		static const std::map<std::pair<State, msgs::MessageType>, State> kSuccess_Transitions;
		static const std::map<std::pair<State, msgs::MessageType>, t_Handler> kHandlers;

		I_ServerOps &_server;
		std::shared_ptr<Client> _client;

		StateMachine(I_ServerOps &server, std::shared_ptr<Client> client);
		void Run();

		bool Handle_Nickname_Set(const msgs::Message &msg);
		bool Handle_Room_Create(const msgs::Message &msg);
		bool Handle_Room_Join(const msgs::Message &msg);
		bool Handle_Room_Leave(const msgs::Message &msg);
		bool Handle_Board_Ready(const msgs::Message &msg);
		bool Handle_Turn(const msgs::Message &msg);
	};

} // game
