#pragma once

#include "../ntwrk/Socket.hpp"
#include "../msgs/Message.hpp"
#include "../msgs/Communicator.hpp"
#include "I_ClientOps.hpp"

#include <chrono>


namespace game {

	/// Client's State Machine State
	enum class State {
		kInit,
		kIn_Lobby,
		kIn_Room,
		kIn_Game
	};

	std::string Get_Logger_State_Description(State state);
	std::string Get_Logger_Time_Point_Description(const std::chrono::time_point<std::chrono::steady_clock> &time_point);

	class Client : public I_ClientOps {
	public:
		/// Transparently constructs
		/// \param sock Socket
		explicit Client(std::unique_ptr<ntwrk::Socket> sock);

		Client(const Client &other) = delete;
		Client &operator=(const Client &other) = delete;

		/// Returns whether the clients are equal (have same nicknames)
		/// \param lhs lhs
		/// \param rhs rhs
		/// \return Bool
		friend bool operator==(const Client &lhs, const Client &rhs);

		State Get_State() const override;
		void Set_State(State state) override;

		const std::chrono::time_point<std::chrono::steady_clock> &Get_Last_Active() const override;
		void Set_Last_Active(const std::chrono::time_point<std::chrono::steady_clock> &time_point) override;

		const std::string &Get_Nickname() const override;
		void Set_Nickname(const std::string &nickname) override;

		msgs::Message Recv_Msg() const override;
		void Send_Msg(const msgs::Message &msg) const override;
		void Send_Ack() const override;

		std::unique_ptr<ntwrk::Socket> Give_Up_Socket() override;
		void Replace_Socket(std::unique_ptr<ntwrk::Socket> sock) override;
		void Close_Socket() override;

	private:
		std::unique_ptr<ntwrk::Socket> _sock;

		std::string _nickname;
		State _state;
		std::chrono::time_point<std::chrono::steady_clock> _last_active;
	};

} // game
