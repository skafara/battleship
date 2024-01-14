#pragma once

#include <string>
#include <sstream>

#include "../ntwrk/Socket.hpp"
#include "Message.hpp"


namespace msgs {

	/// Messages Communicator
	class Communicator {
	public:
		/// Sends the provided message using provided socket (does not block)
		/// Escapes message delimiters (and escape characters)
		/// Does not throw if socket is closed by the other side or if there is any other problem
		/// \param sock Socket
		/// \param msg Message
		static void Send(const ntwrk::Socket &sock, const Message &msg);
		/// Receives a message using provided socket (blocks)
		/// \param sock Socket
		/// \throws SocketException if socket is closed by the other side or if there is any other problem
		/// \throws IllegalMessageException if it is unexpected/unknown incoming message (type, parameters count)
		static Message Recv(const ntwrk::Socket &sock);

	private:
		static constexpr char kEscape_Char = '\\';
		static constexpr char kMsg_Delimiter = 0x0A;
	};

} // msgs
