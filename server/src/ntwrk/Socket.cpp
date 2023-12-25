#include <unistd.h>
#include <fcntl.h>

#include "Socket.hpp"


namespace ntwrk {

	Socket::Socket(int fd) : _fd(fd) {
		if (fcntl(_fd, F_GETFD) == -1) {
			throw -1;
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

} // ntwrk