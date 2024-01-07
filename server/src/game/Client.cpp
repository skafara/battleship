#include "Client.hpp"
#include "../msgs/Messages.hpp"


namespace game {

	Client::Client(std::unique_ptr<ntwrk::Socket> sock) : _sock(std::move(sock)) {
		//
	}

	/*Client::Client(Client &&other) noexcept :
		_sock(std::move(other._sock)),
		_nickname(std::move(other._nickname)),
		_state(other._state),
		_last_active(other._last_active) {
		//
	}

	Client &Client::operator=(Client &&other) noexcept {
		_sock = std::move(other._sock);
		_nickname = std::move(other._nickname);
		_state = other._state;
		_last_active = other._last_active;

		return *this;
	}*/

	bool operator==(const Client &lhs, const Client &rhs) {
		return lhs._nickname == rhs._nickname;
	}

	/*std::mutex &Client::Get_Mutex_State() const {
		return _mutex_state;
	}*/

	State Client::Get_State() const {
		return _state;
	}

	void Client::Set_State(State state) {
		_state = state;
	}

	const std::chrono::time_point<std::chrono::steady_clock> &Client::Get_Last_Active() const {
		return _last_active;
	}

	void Client::Set_Last_Active(const std::chrono::time_point<std::chrono::steady_clock> &time_point) {
		_last_active = time_point;
	}

	const std::string &Client::Get_Nickname() const {
		return _nickname;
	}

	void Client::Set_Nickname(const std::string &nickname) {
		_nickname = nickname;
	}

	void Client::Send_Msg(const msgs::Message &msg) const {
		msgs::Communicator::Send(*_sock, msg);
	}

	void Client::Send_Ack() const {
		Send_Msg(msgs::Messages::Ack());
	}

	msgs::Message Client::Recv_Msg() const {
		return msgs::Communicator::Recv(*_sock);;
	}

	std::unique_ptr<ntwrk::Socket> Client::Give_Up_Socket() {
		return std::move(_sock);
	}

	void Client::Replace_Socket(std::unique_ptr<ntwrk::Socket> sock) {
		_sock = std::move(sock);
	}

	void Client::Close_Socket() {
		_sock = nullptr;
	}

	/*void Client::Await_Ack() const {
		Await_Msg({msgs::MessageType::kAck});
	}*/

} // game