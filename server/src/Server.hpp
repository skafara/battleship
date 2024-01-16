#pragma once

#include <string>
#include <map>

#include "ntwrk/SocketAcceptor.hpp"
#include "game/Client.hpp"
#include "I_ServerOps.hpp"
#include "game/StateMachine.hpp"

/// Server
class Server : public I_ServerOps {
public:
	/// Constructs a server
	/// \param addr Address
	/// \param port Port
	/// \param lim_clients Clients count limit
	/// \param lim_rooms Rooms count limit
	Server(const std::string &addr, uint16_t port, size_t lim_clients, size_t lim_rooms);

	/// Serve Loop
	void Serve();

	std::mutex &Get_Mutex() const override;

	size_t Get_Lim_Rooms() const override;
	bool Is_Reached_Lim_Rooms() const override;
	const std::string &Create_Room() override;

	bool Exists_Room(const std::string &code) const override;
	std::shared_ptr<game::Room> Get_Room(const std::string &code) const override;
	std::shared_ptr<game::Room> Get_Room(const std::shared_ptr<game::Client> client) const override;

	void Destroy_Room(const std::shared_ptr<game::Room> room) override;
	void Erase_Disconnected_Client(const std::string &nickname) override;

	bool Is_Nickname_Connected(const std::string &nickname) const override;
	bool Is_Nickname_Disconnected(const std::string &nickname) const override;

	void Disconnect_Client(const std::shared_ptr<game::Client> client) override;
	void Reconnect_Client(std::shared_ptr<game::Client> &client) override;

private:
	static constexpr bool kIs_Timeout_Debug = true;
	static constexpr std::chrono::minutes Timeout_Long{kIs_Timeout_Debug ? 15 : 2};
	static constexpr bool kIs_Interval_Debug = true;
	static constexpr std::chrono::seconds Interval_Keep_Alive{kIs_Interval_Debug ? 60 : 5};

	const size_t _lim_clients;
	const size_t _lim_rooms;

	ntwrk::SocketAcceptor _sock_acceptor;

	std::vector<std::shared_ptr<game::Client>> _clients; /// Connected clients pool
	std::vector<std::shared_ptr<game::Client>> _disconnected; /// Disconnected clients pool
	std::vector<std::shared_ptr<game::Room>> _rooms; /// Rooms

	mutable std::mutex _mutex;

	std::unique_ptr<ntwrk::Socket> Accept_Connection() const;
	void Refuse_Connection(std::unique_ptr<ntwrk::Socket> sock) const;

	/// Serve Client Loop
	/// \param client Client
	void Serve_Client(std::shared_ptr<game::Client> client);
	void Clients_Alive_Keeper() const; /// Keep Alive Loop
	void Clients_Terminator(); /// Terminator Loop
};
