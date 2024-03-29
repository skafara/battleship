#include "StateMachine.hpp"

#include "../msgs/Messages.hpp"

#include "thread"
#include "iostream"
#include "../util/Logger.hpp"
#include <future>
namespace game {

	const std::map<State, std::set<msgs::MessageType>> StateMachine::kExpected_Msgs{
		{State::kInit, {msgs::MessageType::kNickname_Set}},
		{State::kIn_Lobby, {msgs::MessageType::kRoom_Create, msgs::MessageType::kRoom_Join}},
		{State::kIn_Room, {msgs::MessageType::kRoom_Leave, msgs::MessageType::kBoard_Ready}},
		{State::kIn_Game, {msgs::MessageType::kRoom_Leave, msgs::MessageType::kTurn}}
	};

	const std::map<std::pair<State, msgs::MessageType>, State> StateMachine::kSuccess_Transitions{
		{{State::kInit, msgs::MessageType::kNickname_Set}, State::kIn_Lobby},
		{{State::kIn_Lobby, msgs::MessageType::kRoom_Create}, State::kIn_Room},
		{{State::kIn_Lobby, msgs::MessageType::kRoom_Join}, State::kIn_Room},
		{{State::kIn_Room, msgs::MessageType::kRoom_Leave}, State::kIn_Lobby},
		{{State::kIn_Room, msgs::MessageType::kBoard_Ready}, State::kIn_Room},
		{{State::kIn_Game, msgs::MessageType::kRoom_Leave}, State::kIn_Lobby},
		{{State::kIn_Game, msgs::MessageType::kTurn}, State::kIn_Game}
	};

	const std::map<std::pair<State, msgs::MessageType>, t_Handler> StateMachine::kHandlers{
		{{State::kInit, msgs::MessageType::kNickname_Set}, &StateMachine::Handle_Nickname_Set},
		{{State::kIn_Lobby, msgs::MessageType::kRoom_Create}, &StateMachine::Handle_Room_Create},
		{{State::kIn_Lobby, msgs::MessageType::kRoom_Join}, &StateMachine::Handle_Room_Join},
		{{State::kIn_Room, msgs::MessageType::kRoom_Leave}, &StateMachine::Handle_Room_Leave},
		{{State::kIn_Room, msgs::MessageType::kBoard_Ready}, &StateMachine::Handle_Board_Ready},
		{{State::kIn_Game, msgs::MessageType::kRoom_Leave}, &StateMachine::Handle_Room_Leave},
		{{State::kIn_Game, msgs::MessageType::kTurn}, &StateMachine::Handle_Turn}
	};

	void StateMachine::Run(I_ServerOps &server, std::shared_ptr<Client> client) {
		StateMachine{server, client}.Run();
	}

	StateMachine::StateMachine(I_ServerOps &server, std::shared_ptr<game::Client> client) :
		_server(server), _client(client) {
		//
	}

	void StateMachine::Run() {
		util::Logger::Info("StateMachine.Run");
		for (;;) {
			try {
				const msgs::Message msg = Await_Msg();
				util::Logger::Info("Received Msg: " + msg.Serialize());

				std::lock_guard lck{_server.Get_Mutex()};

				if (!kExpected_Msgs.at(_client->Get_State()).contains(msg.Get_Type())) {
					throw msgs::IllegalMessageException{"Illegal Client State Message"};
				}

				const std::pair<State, msgs::MessageType> pair{_client->Get_State(), msg.Get_Type()};
				const t_Handler handler = kHandlers.at(pair);
				util::Logger::Trace("Handle Msg: " + msg.Serialize());
				if (handler(*this, msg)) {
					_client->Set_State(kSuccess_Transitions.at({_client->Get_State(), msg.Get_Type()}));
				}
			}
			catch (const ntwrk::SocketException &e) {
				util::Logger::Error(e.what());
				std::lock_guard lck{_server.Get_Mutex()};

				_server.Disconnect_Client(_client);
				return;
			}
			catch (const msgs::IllegalMessageException &e) {
				util::Logger::Error(e.what());
				std::lock_guard lck{_server.Get_Mutex()};

				_server.Disconnect_Client(_client);
				return;
			}
			catch (const TimeoutException &e) {
				util::Logger::Error(e.what());
				std::lock_guard lck{_server.Get_Mutex()};

				_server.Disconnect_Client(_client);
				return;
			}
		}
	}

	msgs::Message StateMachine::Await_Msg() const {
		util::Logger::Info("Await Msg");
		for (;;) {
			std::promise<msgs::Message> promise;
			std::future<msgs::Message> future = promise.get_future();

			const auto Get_Msg = [](const Client &client,std::promise<msgs::Message> &promise) {
				try {
					const msgs::Message msg = client.Recv_Msg();
					promise.set_value(msg);
				}
				catch (const msgs::IllegalMessageException &e) {
					promise.set_exception(std::make_exception_ptr(e));
				}
				catch (const ntwrk::SocketException &e) {
					promise.set_exception(std::make_exception_ptr(e));
				}
			};

			std::thread thread{Get_Msg, std::ref(*_client), std::ref(promise)};

			auto future_status = future.wait_until(_client->Get_Last_Active() + Timeout_Short);
			if (future_status == std::future_status::timeout) {
				// ! Linux specific ! //
				const pthread_t handle = thread.native_handle();
				pthread_cancel(handle);

				thread.join();
				throw TimeoutException("Receive Message Timed Out");
			}

			util::Logger::Info("Received Msg In Time");
			thread.join();
			_client->Set_Last_Active(std::chrono::steady_clock::now());
			const msgs::Message msg = future.get();
			if (msg.Get_Type() == msgs::MessageType::kKeep_Alive) {
				util::Logger::Info("Received Msg: " + msg.Serialize());
				continue;
			}

			return msg;
		}
	}

	bool StateMachine::Handle_Nickname_Set(const msgs::Message &msg) {
		const std::string &nickname = msg.Get_Param(0);

		if (_server.Is_Nickname_Connected(nickname)) {
			util::Logger::Trace("Nickname Active");
			_client->Send_Msg(msgs::Messages::Nickname_Exists());
			return false;
		}

		if (_server.Is_Nickname_Disconnected(nickname)) {
			util::Logger::Trace("Nickname Disconnected");
			_client->Set_Nickname(nickname);
			_server.Reconnect_Client(_client);
			return false;
		}

		_client->Set_Nickname(nickname);
		_client->Send_Ack();
		return true;
	}

	bool StateMachine::Handle_Room_Create(const msgs::Message &msg) {
		if (_server.Is_Reached_Lim_Rooms()) {
			util::Logger::Trace("Room Limit Exceeded");
			_client->Send_Msg(msgs::Messages::Limit_Rooms(_server.Get_Lim_Rooms()));
			return false;
		}

		const std::string &code = _server.Create_Room();
		const std::shared_ptr<Room> room = _server.Get_Room(code);

		room->Join(_client);
		_client->Send_Msg(msgs::Messages::Room_Created(code));
		return true;
	}

	bool StateMachine::Handle_Room_Join(const msgs::Message &msg) {
		const std::string &code = msg.Get_Param(0);

		if (!_server.Exists_Room(code)) {
			util::Logger::Trace("Room Not Exists");
			_client->Send_Msg(msgs::Messages::Room_Not_Exists());
			return false;
		}

		const std::shared_ptr<Room> room = _server.Get_Room(code);
		if (room->Is_Full()) {
			util::Logger::Trace("Room Full");
			_client->Send_Msg(msgs::Messages::Room_Full());
			return false;
		}

		room->Join(_client);
		_client->Send_Ack();

		const Client &opponent = room->Get_Opponent(*_client);
		_client->Send_Msg(msgs::Messages::Opponent_Nickname_Set(opponent.Get_Nickname()));
		opponent.Send_Msg(msgs::Messages::Opponent_Nickname_Set(_client->Get_Nickname()));

		if (room->Is_Board_Ready(opponent)) {
			_client->Send_Msg(msgs::Messages::Opponent_Board_Ready());
		}

		return true;
	}

	bool StateMachine::Handle_Room_Leave(const msgs::Message &msg) {
		const std::shared_ptr<Room> room = _server.Get_Room(_client);

		_client->Send_Msg(msgs::Messages::Ack());
		_client->Set_State(State::kIn_Lobby);

		if (room->Is_Full()) {
			Client &opponent = room->Get_Opponent(*_client);

			opponent.Send_Msg(msgs::Messages::Opponent_Room_Leave());
			opponent.Set_State(State::kIn_Lobby);

			if (_server.Is_Nickname_Disconnected(opponent.Get_Nickname())) {
				_server.Erase_Disconnected_Client(opponent.Get_Nickname());
			}
		}

		_server.Destroy_Room(room);

		return false;
	}

	bool StateMachine::Handle_Board_Ready(const msgs::Message &msg) {
		const std::shared_ptr<Room> room = _server.Get_Room(_client);
		if (room->Is_Board_Ready(*_client)) {
			throw msgs::IllegalMessageException{"Illegal Board Ready Message State"};
		}

		try {
			Board board{};
			for (size_t i = 0; i < Board::kShip_Fields_Cnt; ++i) {
				const std::string &field = msg.Get_Param(i);
				const std::pair<size_t, size_t> field_pos = Board::Deserialize_Field(field);
				board.Set_Ship(field_pos.first, field_pos.second);
			}

			if (!board.Is_Valid()) {
				throw std::invalid_argument{"Invalid Board"};
			}

			room->Set_Board(*_client, board);
			room->Set_Board_Ready(*_client);
			_client->Send_Ack();
		}
		catch (const std::invalid_argument &) {
			util::Logger::Trace("Board Illegal");
			_client->Send_Msg(msgs::Messages::Board_Illegal());
			return false;
		}

		if (!room->Is_Full()) {
			return true;
		}

		Client &opponent = room->Get_Opponent(*_client);
		opponent.Send_Msg(msgs::Messages::Opponent_Board_Ready());

		if (room->Is_Board_Ready(opponent)) {
			util::Logger::Info("Game Begin " + room->Get_Code());
			_client->Send_Msg(msgs::Messages::Game_Begin());
			opponent.Send_Msg(msgs::Messages::Game_Begin());

			_client->Set_State(State::kIn_Game);
			opponent.Set_State(State::kIn_Game);

			room->Set_Random_Client_On_Turn();

			const Client &client_on_turn = room->Get_Client_On_Turn();
			client_on_turn.Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kYou));
			room->Get_Opponent(client_on_turn).Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kOpponent));

			return false;
		}

		return true;
	}

	bool StateMachine::Handle_Turn(const msgs::Message &msg) {
		const std::shared_ptr<Room> room = _server.Get_Room(_client);
		if (!room->Is_On_Turn(*_client)) {
			util::Logger::Trace("Not Your Turn");
			_client->Send_Msg(msgs::Messages::Turn_Not_You());
			return false;
		}

		const std::string &field = msg.Get_Param(0);
		try {
			const std::pair<size_t, size_t> field_pos = Board::Deserialize_Field(field);

			Client &opponent = room->Get_Opponent(*_client);
			Board &board = room->Get_Board(opponent);
			if (board.Is_Guess(field_pos.first, field_pos.second)) {
				throw std::invalid_argument{"Guessing Previously Guessed Field"};
			}
			if (board.Is_Invalidated(field_pos.first, field_pos.second)) {
				throw std::invalid_argument{"Guessing Previously Invalidated Field"};
			}

			if (board.Turn(field_pos.first, field_pos.second)) {
				util::Logger::Info("Hit");
				_client->Send_Msg(msgs::Messages::Turn_Result(field_pos.first, field_pos.second, msgs::Messages::Turn_Res::kHit));
				opponent.Send_Msg(msgs::Messages::Opponent_Turn(field_pos.first, field_pos.second, msgs::Messages::Turn_Res::kHit));

				for (const std::pair<size_t, size_t> &invalidated_field_pos : board.Get_Latest_Invalidated()) {
					_client->Send_Msg(msgs::Messages::Invalidate_Field(msgs::Messages::Client::kOpponent, invalidated_field_pos.first, invalidated_field_pos.second));
					opponent.Send_Msg(msgs::Messages::Invalidate_Field(msgs::Messages::Client::kYou, invalidated_field_pos.first, invalidated_field_pos.second));
				}

				if (board.Is_All_Ships_Guessed()) {
					util::Logger::Info("Game End " + room->Get_Code() + _client->Get_Nickname());

					_client->Send_Msg(msgs::Messages::Game_End(msgs::Messages::Client::kYou));
					opponent.Send_Msg(msgs::Messages::Game_End(msgs::Messages::Client::kOpponent));

					_client->Set_State(State::kIn_Room);
					opponent.Set_State(State::kIn_Room);
					room->Reset_Boards();
					return false;
				}
			}
			else {
				util::Logger::Info("Miss");
				_client->Send_Msg(msgs::Messages::Turn_Result(field_pos.first, field_pos.second, msgs::Messages::Turn_Res::kMiss));
				opponent.Send_Msg(msgs::Messages::Opponent_Turn(field_pos.first, field_pos.second, msgs::Messages::Turn_Res::kMiss));

				room->Set_Opponent_On_Turn(*_client);
				_client->Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kOpponent));
				opponent.Send_Msg(msgs::Messages::Turn_Set(msgs::Messages::Client::kYou));
				return false;
			}
		}
		catch (const std::invalid_argument &e) {
			util::Logger::Trace(e.what());
			_client->Send_Msg(msgs::Messages::Turn_Illegal());
			return false;
		}

		return true;
	}

} // game