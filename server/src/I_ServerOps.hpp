#pragma once

#include <mutex>

#include "game/Room.hpp"
#include "game/StateMachine.hpp"


namespace game {
	enum class State;
}

/// Server Operations
class I_ServerOps {
public:
	/// Returns server mutex for mutex-operations
	/// \return Mutex
	virtual std::mutex &Get_Mutex() const = 0;

	/// Returns rooms count limit
	/// \return Limit
	virtual size_t Get_Lim_Rooms() const = 0;
	/// Returns whether rooms count limit has been reached
	/// \return Bool
	virtual bool Is_Reached_Lim_Rooms() const = 0;
	/// Creates a room
	/// \return Room code
	virtual const std::string &Create_Room() = 0;

	/// Returns whether a room with provided code exists
	/// \param code Room code
	/// \return Bool
	virtual bool Exists_Room(const std::string &code) const = 0;
	/// Returns a room given provided code. Room must exist
	/// \param code Room code
	/// \return Room
	virtual std::shared_ptr<game::Room> Get_Room(const std::string &code) const = 0;
	/// Returns a room given provided client or nullptr if client is not in any room
	/// \param code Client
	/// \return Room
	virtual std::shared_ptr<game::Room> Get_Room(const std::shared_ptr<game::Client> client) const = 0;

	/// Destroys a room
	/// \param room Room
	virtual void Destroy_Room(const std::shared_ptr<game::Room> room) = 0;
	/// Erases a disconnected client with provided nickname from disconnected clients pool
	/// \param nickname Nickname
	virtual void Erase_Disconnected_Client(const std::string &nickname) = 0;

	/// Returns whether a client with provided nickname is connected
	/// \param nickname Nickname
	/// \return Bool
	virtual bool Is_Nickname_Connected(const std::string &nickname) const = 0;
	/// Returns whether a client with provided nickname is disconnected (in disconnected clients pool)
	/// \param nickname Nickname
	/// \return Bool
	virtual bool Is_Nickname_Disconnected(const std::string &nickname) const = 0;

	/// Disconnects a client (moves it to the disconnected clients pool)
	/// \param client Client
	virtual void Disconnect_Client(const std::shared_ptr<game::Client> client) = 0;
	/// Reconnects a client
	/// \param client Client
	virtual void Reconnect_Client(std::shared_ptr<game::Client> &client) = 0;
};
