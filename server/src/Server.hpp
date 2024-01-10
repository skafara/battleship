#pragma once

#include <string>
#include <map>
#include <mutex>

#include "ntwrk/SocketAcceptor.hpp"
#include "game/Client.hpp"
#include "I_ServerOps.hpp"
#include "game/StateMachine.hpp"


class Server : public I_ServerOps {
public:
	Server(const std::string &addr, uint16_t port, size_t lim_clients, size_t lim_rooms);

	void Serve();

	std::mutex &Get_Mutex() const override;

	size_t Get_Lim_Rooms() const override;
	bool Is_Exceeded_Lim_Rooms() const override;
	const std::string &Create_Room() override; // lock, throw exception

	bool Exists_Room(const std::string &code) const override;
	std::shared_ptr<game::Room> Get_Room(const std::string &code) const override;
	std::shared_ptr<game::Room> Get_Room(const std::shared_ptr<game::Client> client) const override; // lock

	void Destroy_Room(const std::shared_ptr<game::Room> room) override;
	void Erase_Disconnected_Client(const std::string &nickname) override;

	bool Is_Nickname_Active(const std::string &nickname) const override;
	bool Is_Nickname_Disconnected(const std::string &nickname) const override;

	void Disconnect_Client(const std::shared_ptr<game::Client> client) override;
	void Reconnect_Client(std::shared_ptr<game::Client> &client) override;

private:
	static constexpr std::chrono::seconds Timeout_Long{6000};

	const size_t _lim_clients;
	const size_t _lim_rooms;

	ntwrk::SocketAcceptor _sock_acceptor;

	std::vector<std::shared_ptr<game::Client>> _clients;
	std::vector<std::shared_ptr<game::Client>> _disconnected;
	std::vector<std::shared_ptr<game::Room>> _rooms;

	mutable std::mutex _mutex;

	std::unique_ptr<ntwrk::Socket> Accept_Connection() const;
	void Refuse_Connection(std::unique_ptr<ntwrk::Socket> sock) const;

	void Clients_Terminator();

	void Serve_Client(std::shared_ptr<game::Client> client);
	void Terminate_Client(std::shared_ptr<game::Client> client);
};
