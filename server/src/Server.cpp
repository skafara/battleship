#include "Server.hpp"

#include "msgs/MsgCommunicator.hpp"

#include <iostream>
#include <thread>


Server::Server(const std::string &addr, uint16_t port, size_t lim_clients, size_t lim_rooms) :
		_lim_clients(lim_clients), _lim_rooms(lim_rooms), _sock_acceptor({addr, port}), _clients_cnt(0) {
	//
}

void Server::Serve() {
	for (;;) {
		ntwrk::Socket sock = _sock_acceptor.Accept();

		if (_clients_cnt >= _lim_clients) {
			Refuse_Client(std::move(sock));
			continue;
		}

		_clients_cnt++;
		std::thread client_thread{&Server::Serve_Client, this, std::move(sock)};
		client_thread.detach(); // TODO rozmyslet thread flow
	}
}

void Server::Refuse_Client(ntwrk::Socket sock) const {
	msgs::MsgCommunicator msgc{sock};
	msgc.Send(1);
}

void Server::Serve_Client(ntwrk::Socket sock) const {
}