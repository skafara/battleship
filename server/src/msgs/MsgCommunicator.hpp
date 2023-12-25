#pragma once

#include "../ntwrk/Socket.hpp"


namespace msgs {

	class MsgCommunicator {
	public:
		MsgCommunicator(ntwrk::Socket &sock);

		void Send(int i);
		int Recv();
	};

} // msgs
