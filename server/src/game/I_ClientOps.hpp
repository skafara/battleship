#pragma once

#include <set>
#include <chrono>
#include <memory>

#include "../msgs/Message.hpp"


namespace game {

	enum class State;
	class I_ClientOps {
	public:
		/// Returns client's state machine state
		/// \return Client's state machine state
		virtual State Get_State() const = 0;
		/// Sets client's state machine state
		/// \param state Client's state machine state
		virtual void Set_State(State state) = 0;

		/// Gets last active time point (last received message)
		/// \return Last active time point
		virtual const std::chrono::time_point<std::chrono::steady_clock> &Get_Last_Active() const = 0;
		/// Sets last active time point (last received message)
		/// \param time_point Last active time point
		virtual void Set_Last_Active(const std::chrono::time_point<std::chrono::steady_clock> &time_point) = 0;

		/// Returns nickname
		/// \return Nickname
		virtual const std::string &Get_Nickname() const = 0;
		/// Sets nickname
		/// \param nickname Nickname
		virtual void Set_Nickname(const std::string &nickname) = 0;

		/// Receives a message (blocks)
		/// \throws SocketException if socket is closed by the other side or if there is any other problem
		/// \throws IllegalMessageException if it is unexpected/unknown incoming message (type, parameters count)
		virtual msgs::Message Recv_Msg() const = 0;

		/// Sends the provided message (does not block)
		/// Does not throw if socket is closed by the other side or if there is any other problem
		/// \param msg Message
		virtual void Send_Msg(const msgs::Message &msg) const = 0;
		/// Sends ACK message (does not block)
		/// Does not throw if socket is closed by the other side or if there is any other problem
		virtual void Send_Ack() const = 0;

		/// Gives up the socket
		/// \return Socket
		virtual std::unique_ptr<ntwrk::Socket> Give_Up_Socket() = 0;
		/// Replaces the socket with provided socket
		/// \param sock Socket
		virtual void Replace_Socket(std::unique_ptr<ntwrk::Socket> sock) = 0;
		/// Closes the socket
		virtual void Close_Socket() = 0;
	};

}
