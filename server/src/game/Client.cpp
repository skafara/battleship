#include "Client.hpp"
#include "../msgs/Messages.hpp"


namespace game {

	Client::Client(std::unique_ptr<ntwrk::Socket> sock) : _sock(std::move(sock)), _msgc(*_sock) {
		//
	}

	bool operator==(const Client &lhs, const Client &rhs) {
		return lhs._nickname == rhs._nickname;
	}

	const std::string &Client::Get_Nickname() const {
		return _nickname;
	}

	void Client::Set_Nickname(const std::string &nickname) {
		_nickname = nickname;
	}

	void Client::Send_Msg(const msgs::Message &msg) const {
		_msgc.Send(msg);
	}

	void Client::Send_Ack() const {
		Send_Msg(msgs::Messages::Ack());
	}

	msgs::Message Client::Recv_Msg() const {
		return _msgc.Recv();
	}

	msgs::Message Client::Await_Msg(const std::set<msgs::MessageType> &expected) const {
		for (;;) {
			const msgs::Message msg = Recv_Msg();

			if (msg.Get_Type() == msgs::MessageType::kPong) {
				continue;
			}
			if (!expected.contains(msg.Get_Type())) {
				throw -1;
			}

			return msg;
		}
	}

	void Client::Await_Ack() const {
		Await_Msg({msgs::MessageType::kAck});
	}

} // game