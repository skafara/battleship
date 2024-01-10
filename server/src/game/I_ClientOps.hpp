#pragma once

#include <set>
#include <chrono>
#include <memory>

#include "../msgs/Message.hpp"


namespace game {

	enum class State;
	class I_ClientOps {
	public:
		//virtual std::mutex &Get_Mutex_State() const = 0;
		virtual State Get_State() const = 0;
		virtual void Set_State(State state) = 0;

		virtual const std::chrono::time_point<std::chrono::steady_clock> &Get_Last_Active() const = 0;
		virtual void Set_Last_Active(const std::chrono::time_point<std::chrono::steady_clock> &time_point) = 0;

		virtual const std::string &Get_Nickname() const = 0;
		virtual void Set_Nickname(const std::string &nickname) = 0;

		virtual msgs::Message Recv_Msg() const = 0;

		virtual void Send_Msg(const msgs::Message &msg) const = 0;
		virtual void Send_Ack() const = 0;

		virtual std::unique_ptr<ntwrk::Socket> Give_Up_Socket() = 0;
		virtual void Replace_Socket(std::unique_ptr<ntwrk::Socket> sock) = 0;
		virtual void Close_Socket() = 0;

		//virtual msgs::Message Await_Msg() const = 0;
		//virtual void Await_Ack() const = 0;
	};

}
