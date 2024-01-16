#pragma once

#include <map>
#include <functional>
#include <exception>

#include "../msgs/Message.hpp"
#include "../I_ServerOps.hpp"


class I_ServerOps;

namespace game {

	/// Timeout Exception
	class TimeoutException : public std::runtime_error {
	public:
		/// Transparently constructs
		explicit TimeoutException(const std::string &text) : std::runtime_error(text) {
			//
		}
	};

	class StateMachine;
	/// Message Handler Type
	using t_Handler = std::function<bool (StateMachine &, const msgs::Message &)>;

	/// State Machine
	class StateMachine {
	public:
		/// Constructs and runs a state machine with provided client
		/// \param server Server
		/// \param client Client
		static void Run(I_ServerOps &server, std::shared_ptr<Client> client);

	private:
		static const std::map<State, std::set<msgs::MessageType>> kExpected_Msgs;
		static const std::map<std::pair<State, msgs::MessageType>, State> kSuccess_Transitions;
		static const std::map<std::pair<State, msgs::MessageType>, t_Handler> kHandlers;

		static constexpr bool kIs_Timeout_Debug = false;
		static constexpr std::chrono::seconds Timeout_Short{kIs_Timeout_Debug ? 300 : 15};

		I_ServerOps &_server;
		std::shared_ptr<Client> _client;

		StateMachine(I_ServerOps &server, std::shared_ptr<Client> client);
		void Run();

		/// Awaits Message (skips Keep Alive messages)
		/// \throws TimeoutException on message receive timeout
		/// \return Message
		msgs::Message Await_Msg() const;

		bool Handle_Nickname_Set(const msgs::Message &msg);
		bool Handle_Room_Create(const msgs::Message &msg);
		bool Handle_Room_Join(const msgs::Message &msg);
		bool Handle_Room_Leave(const msgs::Message &msg);
		bool Handle_Board_Ready(const msgs::Message &msg);
		bool Handle_Turn(const msgs::Message &msg);
	};

} // game
