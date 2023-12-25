#include "SocketAcceptor.hpp"

#include <unistd.h>
#include <sys/socket.h>
#include <netdb.h>


namespace ntwrk {

	SocketAcceptor::SocketAcceptor(const std::string &addr, uint16_t port, int backlog) {
		_socket = socket(AF_INET, SOCK_STREAM, 0);
		if (_socket == -1) {
			throw -1; // TODO
		}

		int reuse = 1; // chatgpt
		if (setsockopt(_socket, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse)) < 0) { //chatgpt
			// Handle the error
		}

		sockaddr_in sock_addr{};
		sock_addr.sin_family = AF_INET;
		sock_addr.sin_port = htons(port);
		sock_addr.sin_addr.s_addr = Compute_Sock_Addr(addr);

		if (bind(_socket, reinterpret_cast<sockaddr *>(&sock_addr), sizeof(sock_addr)) == -1) {
			throw -1; // TODO
		}

		if (listen(_socket, backlog) == -1) {
			throw -1; // TODO
		}
	}

	SocketAcceptor::SocketAcceptor(SocketAcceptor &&other) noexcept : _socket(other._socket) {
		other._socket = -1;
	}

	SocketAcceptor &SocketAcceptor::operator=(SocketAcceptor &&other) noexcept {
		_socket = other._socket;

		other._socket = -1;

		return *this;
	}

	SocketAcceptor::~SocketAcceptor() {
		if (_socket != -1) {
			close(_socket);
		}
	}

	Socket SocketAcceptor::Accept() const {
		sockaddr peer_addr{};
		socklen_t peer_addr_len = sizeof(peer_addr);

		const int fd = accept(_socket, &peer_addr, &peer_addr_len);
		if (fd == -1) {
			throw -1; // TODO
		}

		return Socket{fd};
	}

	in_addr_t SocketAcceptor::Compute_Sock_Addr(const std::string &addr) {
		addrinfo hints{}, *res = nullptr;
		hints.ai_family = AF_INET; // IPv4
		hints.ai_socktype = SOCK_STREAM; // TCP

		const int status = getaddrinfo(addr.c_str(), nullptr, &hints, &res);
		if (status != 0) {
			throw -1; // TODO
		}

		const in_addr_t ip = reinterpret_cast<sockaddr_in *>(res->ai_addr)->sin_addr.s_addr;

		freeaddrinfo(res);
		return ip;
	}

} // ntwrk