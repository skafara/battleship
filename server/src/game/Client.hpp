#pragma once

#include "../ntwrk/Socket.hpp"
#include "../msgs/Message.hpp"
#include "../msgs/Communicator.hpp"
#include "I_ClientOps.hpp"

#include <chrono>


namespace game {

	enum class State {
		kInit,
		kIn_Lobby,
		kIn_Room,
		kIn_Game
	};

	class Client : public I_ClientOps {
	public:
		explicit Client(std::unique_ptr<ntwrk::Socket> sock);

		Client(const Client &other) = delete;
		Client &operator=(const Client &other) = delete;

		//Client(Client &&other) noexcept;
		//Client &operator=(Client &&other) noexcept;

		friend bool operator==(const Client &lhs, const Client &rhs);

		//std::mutex &Get_Mutex_State() const override;
		State Get_State() const override;
		void Set_State(State state) override;

		const std::chrono::time_point<std::chrono::steady_clock> &Get_Last_Active() const override;
		void Set_Last_Active(const std::chrono::time_point<std::chrono::steady_clock> &time_point) override;

		const std::string &Get_Nickname() const override;
		void Set_Nickname(const std::string &nickname) override;

		//std::mutex &Get_Mutex_Sock() const {return _mutex_sock;}; // TODO cli ops

		msgs::Message Recv_Msg() const override;
		void Send_Msg(const msgs::Message &msg) const override;
		void Send_Ack() const override;

		std::unique_ptr<ntwrk::Socket> Give_Up_Socket() override;
		void Replace_Socket(std::unique_ptr<ntwrk::Socket> sock) override;
		void Close_Socket() override;

		//msgs::Message Await_Msg() const override;
		//void Await_Ack() const override;

	private:
		std::unique_ptr<ntwrk::Socket> _sock;
		//mutable std::mutex _mutex_sock;
		//msgs::Communicator _msgc;

		std::string _nickname;

		//mutable std::mutex _mutex_state;
		State _state;
		std::chrono::time_point<std::chrono::steady_clock> _last_active;
	};

} // game
