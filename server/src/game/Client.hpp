#pragma once

#include "../ntwrk/Socket.hpp"
#include "../msgs/Message.hpp"
#include "../msgs/Communicator.hpp"
#include "I_ClientOps.hpp"


namespace game {

	class Client : public I_ClientOps {
	public:
		explicit Client(std::unique_ptr<ntwrk::Socket> sock);

		friend bool operator==(const Client &lhs, const Client &rhs);

		const std::string &Get_Nickname() const override;
		void Set_Nickname(const std::string &nickname) override;

		//std::mutex &Get_Mutex_Sock() const {return _mutex_sock;}; // TODO cli ops

		void Send_Msg(const msgs::Message &msg) const override;
		void Send_Ack() const override;

		msgs::Message Await_Msg(const std::set<msgs::MessageType> &expected) const override;
		void Await_Ack() const override;

	private:
		const std::unique_ptr<ntwrk::Socket> _sock;
		//mutable std::mutex _mutex_sock;
		const msgs::Communicator _msgc;

		std::string _nickname;

		msgs::Message Recv_Msg() const;
	};

} // game
