#pragma once

#include <cstddef>
#include <cstdint>
#include <stdexcept>
#include <string>


namespace ntwrk {

	class SocketException : public std::runtime_error {
	public:
		explicit SocketException(const std::string &text) : std::runtime_error(text) {
			//
		}
	};

	class Socket {
	public:
		explicit Socket(int fd);

		Socket(const Socket &other) = delete;
		Socket &operator=(const Socket &other) = delete;

		Socket(Socket &&other) noexcept;
		Socket &operator=(Socket &&other) noexcept;

		~Socket();

		std::byte Read_Byte() const;
		void Write_Byte(std::byte byte) const;

	private:
		int _fd;
	};

} // ntwrk
