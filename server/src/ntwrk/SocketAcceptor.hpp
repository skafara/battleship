#pragma once

#include "Socket.hpp"

#include <string>
#include <netinet/in.h>


namespace ntwrk {

	class SocketAcceptor {
	public:
		static constexpr int kDef_Backlog = 5;

		SocketAcceptor(const std::string &addr, uint16_t port, int backlog = kDef_Backlog);

		SocketAcceptor(const SocketAcceptor &other) = delete;
		SocketAcceptor &operator=(const SocketAcceptor &other) = delete;

		/*SocketAcceptor(SocketAcceptor &&other) noexcept;
		SocketAcceptor &operator=(SocketAcceptor &&other) noexcept;*/

		~SocketAcceptor();

		Socket Accept() const;

	private:
		int _socket;

		static in_addr_t Compute_Sock_Addr(const std::string &addr);
	};

} // ntwrk
