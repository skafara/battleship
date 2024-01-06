#pragma once

#include <set>

#include "../msgs/Message.hpp"


namespace game {

	enum class State;
	class I_ClientOps {
	public:
		//virtual std::mutex &Get_Mutex_State() const = 0;
		virtual State Get_State() const = 0;
		virtual void Set_State(State state) = 0;

		virtual const std::string &Get_Nickname() const = 0;
		virtual void Set_Nickname(const std::string &nickname) = 0;

		virtual void Send_Msg(const msgs::Message &msg) const = 0;
		virtual void Send_Ack() const = 0;

		virtual msgs::Message Await_Msg() const = 0;
		//virtual void Await_Ack() const = 0;
	};

}
