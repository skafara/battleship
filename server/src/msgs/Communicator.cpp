#include <sstream>

#include "Communicator.hpp"
#include "Message.hpp"


namespace msgs {

	void Communicator::Send(const ntwrk::Socket &sock, const Message &msg) {
		const std::string text = msg.Serialize();

		for (char c : text) {
			if (c == kEscape_Char || c == kMsg_Delimiter) {
				sock.Write_Byte(static_cast<std::byte>(kEscape_Char));
			}
			sock.Write_Byte(static_cast<std::byte>(c));
		}
		sock.Write_Byte(static_cast<std::byte>(kMsg_Delimiter));
	}

	Message Communicator::Recv(const ntwrk::Socket &sock) {
		std::ostringstream osstream{};

		bool escape = false;
		for (char c; ; ) {
			c = static_cast<char>(sock.Read_Byte());

			if (!escape) {
				if (c == kEscape_Char) {
					escape = true;
					continue;
				}

				if (c == kMsg_Delimiter) {
					break;
				}
			}

			escape = false;
			osstream << c;
		}

		return Message::Deserialize(osstream.str());
	}

} // msgs