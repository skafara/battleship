#include <unistd.h>
#include <fcntl.h>

#include "Socket.hpp"


namespace ntwrk {

	Socket::Socket(int fd) : _fd(fd) {
		if (fcntl(_fd, F_GETFD) == -1) {
			throw SocketException{"Invalid File Descriptor"};
		}
	}

	Socket::Socket(Socket &&other) noexcept : _fd(other._fd) {
		other._fd = -1;
	}

	Socket &Socket::operator=(Socket &&other) noexcept {
		_fd = other._fd;

		other._fd = -1;

		return *this;
	}

	Socket::~Socket() {
		if (_fd != -1) {
			close(_fd);
		}
	}

	std::byte Socket::Read_Byte() const {
		std::byte byte;
		if (read(_fd, &byte, 1) != 1) {
			throw SocketException{"Cannot Read From Socket"};
		}
		return byte;
	}

	void Socket::Write_Byte(std::byte byte) const {
		write(_fd, &byte, 1);
	}

} // ntwrk