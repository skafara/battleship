#include "Client.hpp"
#include "../msgs/Messages.hpp"
#include "../util/Logger.hpp"

#include <iomanip>


namespace game {

	std::string Get_Logger_State_Description(State state) {
		std::string description = "INIT";
		if (state == State::kIn_Lobby) description = "IN_LOBBY";
		else if (state == State::kIn_Room) description = "IN_ROOM";
		else if (state == State::kIn_Game) description = "IN_GAME";
		return description;
	}

	std::string Get_Logger_Time_Point_Description(const std::chrono::time_point<std::chrono::steady_clock> &time_point) {
		const auto time = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now() + duration_cast<std::chrono::system_clock::duration>(time_point - std::chrono::steady_clock::now()));
		const auto *timeinfo = localtime(&time);
		std::ostringstream osstream;
		osstream << std::put_time(timeinfo, "%Y-%m-%d %H:%M:%S");
		return osstream.str();
	}

	Client::Client(std::unique_ptr<ntwrk::Socket> sock) : _sock(std::move(sock)) {
		util::Logger::Trace("Client.Client");
	}

	bool operator==(const Client &lhs, const Client &rhs) {
		return lhs._nickname == rhs._nickname;
	}

	State Client::Get_State() const {
		return _state;
	}

	void Client::Set_State(State state) {
		util::Logger::Info("Client.Set_State " + Get_Logger_State_Description(state));
		_state = state;
	}

	const std::chrono::time_point<std::chrono::steady_clock> &Client::Get_Last_Active() const {
		return _last_active;
	}

	void Client::Set_Last_Active(const std::chrono::time_point<std::chrono::steady_clock> &time_point) {
		util::Logger::Info("Client.Set_Last_Active " + Get_Logger_Time_Point_Description(time_point));
		_last_active = time_point;
	}

	const std::string &Client::Get_Nickname() const {
		return _nickname;
	}

	void Client::Set_Nickname(const std::string &nickname) {
		util::Logger::Info("Client.Set_Nickname " + nickname);
		_nickname = nickname;
	}

	void Client::Send_Msg(const msgs::Message &msg) const {
		util::Logger::Trace("Client.Send_Msg " + msg.Serialize());
		if (!_sock) {
			return;
		}
		msgs::Communicator::Send(*_sock, msg);
	}

	void Client::Send_Ack() const {
		Send_Msg(msgs::Messages::Ack());
	}

	msgs::Message Client::Recv_Msg() const {
		util::Logger::Trace("Client.Recv_Msg");
		if (!_sock) {
			throw ntwrk::SocketException{"Closed Client Socket"};
		}
		return msgs::Communicator::Recv(*_sock);
	}

	std::unique_ptr<ntwrk::Socket> Client::Give_Up_Socket() {
		util::Logger::Trace("Client.Give_Up_Socket");
		return std::move(_sock);
	}

	void Client::Replace_Socket(std::unique_ptr<ntwrk::Socket> sock) {
		util::Logger::Trace("Client.Replace_Socket");
		_sock = std::move(sock);
	}

	void Client::Close_Socket() {
		util::Logger::Trace("Client.Close_Socket");
		_sock = nullptr;
	}

} // game