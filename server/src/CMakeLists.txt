cmake_minimum_required(VERSION 3.0)
project(bserver)

set(CMAKE_CXX_STANDARD 20)

add_executable(bserver main.cpp
        CliArgsParser.cpp
        Server.cpp

        ntwrk/Socket.cpp
        ntwrk/SocketAcceptor.cpp

        msgs/MsgCommunicator.cpp
)
