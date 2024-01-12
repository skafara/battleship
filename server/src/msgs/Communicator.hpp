#pragma once

#include <string>
#include <sstream>

#include "../ntwrk/Socket.hpp"
#include "Message.hpp"


namespace msgs {

	class Communicator {
	public:
		static void Send(const ntwrk::Socket &sock, const Message &msg);
		static Message Recv(const ntwrk::Socket &sock);

	private:
		static constexpr char kEscape_Char = '\\';
		static constexpr char kMsg_Delimiter = 0x0A;
	};

} // msgs
