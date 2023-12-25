#pragma once

#include "../ntwrk/Socket.hpp"


namespace game {

	class Client {
	private:
		ntwrk::Socket _sock;

	public:
		void Send_Msg(int msg);
		int Recv_Msg(); // try?
	};

} // game
