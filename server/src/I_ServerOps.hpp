#pragma once

#include "game/Room.hpp"
#include "game/StateMachine.hpp"

#include <mutex>


namespace game {
	enum class State;
}

class I_ServerOps {
public:
	virtual std::mutex &Get_Mutex() const = 0;

	virtual size_t Get_Lim_Rooms() const = 0;
	virtual bool Is_Exceeded_Lim_Rooms() const = 0;
	virtual const std::string &Create_Room() = 0;

	virtual bool Exists_Room(const std::string &code) const = 0;
	virtual std::shared_ptr<game::Room> Get_Room(const std::string &code) const = 0;
	virtual std::shared_ptr<game::Room> Get_Room(const std::shared_ptr<game::Client> client) const = 0;

	virtual void Destroy_Room(const std::shared_ptr<game::Room> room) = 0;
	virtual void Erase_Disconnected_Client(const std::string &nickname) = 0;

	virtual bool Is_Nickname_Active(const std::string &nickname) const = 0;
	virtual bool Is_Nickname_Disconnected(const std::string &nickname) const = 0;

	virtual void Disconnect_Client(const std::shared_ptr<game::Client> client) = 0;
	virtual void Reconnect_Client(std::shared_ptr<game::Client> &client) = 0;
};
