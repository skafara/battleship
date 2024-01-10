#include "SocketAcceptor.hpp"

#include <unistd.h>
#include <sys/socket.h>
#include <netdb.h>
#include <signal.h>


namespace ntwrk {

	void SocketAcceptor::Initialize() {
		signal(SIGPIPE, SIG_IGN);
	}

	SocketAcceptor::SocketAcceptor(const std::string &addr, uint16_t port, int backlog) {
		_socket = socket(AF_INET, SOCK_STREAM, 0);
		if (_socket == -1) {
			throw SocketException{"Cannot Bind Socket Acceptor"};
		}

		int reuse = 1;
		if (setsockopt(_socket, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse)) < 0) {
			throw SocketException{"Cannot Set Socket Reuse"};
		}

		sockaddr_in sock_addr{};
		sock_addr.sin_family = AF_INET;
		sock_addr.sin_port = htons(port);
		sock_addr.sin_addr.s_addr = Compute_Sock_Addr(addr);

		if (bind(_socket, reinterpret_cast<sockaddr *>(&sock_addr), sizeof(sock_addr)) == -1) {
			throw SocketException{"Cannot Bind Socket Acceptor"};
		}

		if (listen(_socket, backlog) == -1) {
			throw SocketException{"Cannot Listen On Socket Acceptor"};
		}
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
			throw SocketException{"Cannot Accept Socket Connection"};
		}

		return Socket{fd};
	}

	in_addr_t SocketAcceptor::Compute_Sock_Addr(const std::string &addr) {
		addrinfo hints{}, *res = nullptr;
		hints.ai_family = AF_INET; // IPv4
		hints.ai_socktype = SOCK_STREAM; // TCP

		const int status = getaddrinfo(addr.c_str(), nullptr, &hints, &res);
		if (status != 0) {
			throw SocketException{"Cannot Get Address Info For: " + addr};
		}

		const in_addr_t ip = reinterpret_cast<sockaddr_in *>(res->ai_addr)->sin_addr.s_addr;

		freeaddrinfo(res);
		return ip;
	}

} // ntwrk