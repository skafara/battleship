#pragma once

#include <string>
#include <sstream>

#include "../ntwrk/Socket.hpp"
#include "Message.hpp"


namespace msgs {

	class Communicator {
	public:
		explicit Communicator(const ntwrk::Socket &sock);

		/*Communicator(const Communicator &other) = default;
		Communicator &operator=(const Communicator &other) = default;

		Communicator(Communicator &&other) noexcept;
		Communicator &operator=(Communicator &&other) noexcept;*/

		/*void Send(const Message &msg) const;
		Message Recv() const;*/

		static void Send(const ntwrk::Socket &sock, const Message &msg);
		static Message Recv(const ntwrk::Socket &sock);

	private:
		//const ntwrk::Socket &_sock;
	};

} // msgs
