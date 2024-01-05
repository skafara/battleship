#pragma once

#include <set>

#include "../msgs/Message.hpp"


namespace game {

	class I_ClientOps {
	public:
		virtual const std::string &Get_Nickname() const = 0;
		virtual void Set_Nickname(const std::string &nickname) = 0;

		virtual void Send_Msg(const msgs::Message &msg) const = 0;
		virtual void Send_Ack() const = 0;

		virtual msgs::Message Await_Msg(const std::set<msgs::MessageType> &expected) const = 0;
		virtual void Await_Ack() const = 0;
	};

}
