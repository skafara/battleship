#pragma once

#include <string>

#include "ntwrk/SocketAcceptor.hpp"

class Server {
public:
	Server(const std::string &addr, uint16_t port, size_t lim_clients, size_t lim_rooms);

	void Serve();

private:
	const size_t _lim_clients;
	const size_t _lim_rooms;

	ntwrk::SocketAcceptor _sock_acceptor;
	size_t _clients_cnt;

	void Refuse_Client(ntwrk::Socket sock) const;
	void Serve_Client(ntwrk::Socket sock) const;
};
