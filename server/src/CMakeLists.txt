cmake_minimum_required(VERSION 3.0)
project(bserver)

set(CMAKE_CXX_STANDARD 20)

add_executable(bserver main.cpp
        CliArgsParser.cpp
        Server.cpp

        ntwrk/Socket.cpp
        ntwrk/SocketAcceptor.cpp

        msgs/Communicator.cpp
        msgs/Message.cpp
        msgs/Messages.cpp

        game/Client.cpp
        game/StateMachine.cpp
        game/Room.cpp
        game/Board.cpp

        util/Generator.cpp
        util/Logger.cpp
)
target_link_libraries(bserver pthread)
