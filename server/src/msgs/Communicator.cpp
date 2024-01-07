#include <sstream>

#include "Communicator.hpp"
#include "Message.hpp"


namespace msgs {

	/*Communicator::Communicator(const ntwrk::Socket &sock) : _sock(sock) {
		//
	}

	void Communicator::Send(const Message &msg) const {
		Send(_sock, msg);
	}

	Message Communicator::Recv() const {
		return Recv(_sock);
	}*/

	void Communicator::Send(const ntwrk::Socket &sock, const Message &msg) {
		const std::string text = msg.Serialize();

		for (char c : text) {
			if (c == '\\' || c == '\n') {
				sock.Write_Byte(static_cast<std::byte>('\\'));
			}
			sock.Write_Byte(static_cast<std::byte>(c));
		}
		sock.Write_Byte(static_cast<std::byte>('\n'));
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

				if (c == '\n') {
					break;
				}
			}

			escape = false;
			osstream << c;
		}

		return Message::Deserialize(osstream.str());
	}

} // msgs