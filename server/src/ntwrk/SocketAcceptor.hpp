#pragma once

#include "Socket.hpp"

#include <string>
#include <netinet/in.h>


namespace ntwrk {

	/// Socket Connection Acceptor
	class SocketAcceptor {
	public:
		/// Default Backlog
		static constexpr int kDef_Backlog = 5;

		/// Initialization Process
		static void Initialize();

		/// Initializes the acceptor with given parameters
		/// \param addr Address to bind to and listen on for incoming connections
		/// \param port Port to bind to and listen on for incoming connections
		/// \param backlog Incoming connections backlog
		SocketAcceptor(const std::string &addr, uint16_t port, int backlog = kDef_Backlog);

		SocketAcceptor(const SocketAcceptor &other) = delete;
		SocketAcceptor &operator=(const SocketAcceptor &other) = delete;

		~SocketAcceptor();

		/// Returns accepted socket connection
		/// \return Socket
		Socket Accept() const;

	private:
		int _socket;

		static in_addr_t Compute_Sock_Addr(const std::string &addr);
	};

} // ntwrk
