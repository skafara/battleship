#include <sstream>

#include "Communicator.hpp"
#include "Message.hpp"


namespace msgs {

	void Communicator::Send(const ntwrk::Socket &sock, const Message &msg) {
		const std::string text = msg.Serialize();

		for (char c : text) {
			if (c == '\\' || c == 0x0A) {
				sock.Write_Byte(static_cast<std::byte>('\\'));
			}
			sock.Write_Byte(static_cast<std::byte>(c));
		}
		sock.Write_Byte(static_cast<std::byte>(0x0A));
	}

	Message Communicator::Recv(const ntwrk::Socket &sock) {
		std::ostringstream osstream{};

		bool escape = false;
		for (char c; ; ) {
			c = static_cast<char>(sock.Read_Byte());

			if (!escape) {
				if (c == '\\') {
					escape = true;
					continue;
				}

				if (c == 0x0A) {
					break;
				}
			}

			escape = false;
			osstream << c;
		}

		return Message::Deserialize(osstream.str());
	}

} // msgs