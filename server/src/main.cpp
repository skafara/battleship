#include <iostream>

#include "CliArgsParser.hpp"
#include "Server.hpp"

void Print_Info() {
	std::cout << "bserver - Battleship Server v1.0" << std::endl;
	std::cout << "Seminar Work of KIV/UPS, 2023" << std::endl;
	std::cout << "Stanislav Kafara, skafara@students.zcu.cz" << std::endl;
}

void Print_Help() {
	std::cout << "Usage:" << std::endl;
	std::cout << "\tbserver --ip=<ip> --port=<port> --lim-clients=<lim-clients> --lim-rooms=<lim-rooms>" << std::endl;
	std::cout << "\t<ip>\t\t- IP on which the server listens for incoming connections" << std::endl;
	std::cout << "\t<port>\t\t- Port on which the server listens for incoming connections" << std::endl;
	std::cout << "\t<lim-clients>\t- Limit of actively connected clients" << std::endl;
	std::cout << "\t<lim-rooms>\t- Limit of actively used game rooms" << std::endl;
}

int main(int argc, char **argv) {
	Print_Info();

	CliArgsParser::Ignore_Opt_Errs();
	if (CliArgsParser::Is_Help(argc, argv)) {
		Print_Help();
		return EXIT_SUCCESS;
	}

	try {
		CliArgsParser::Result args = CliArgsParser::Parse(argc, argv);

		Server server{args.ip, args.port, args.lim_clients, args.lim_rooms};
		server.Serve();
	} catch (int i) {
		std::cerr << "Err parse" << std::endl;
	} catch (double d) {
		std::cerr << "Server error" << std::endl;
	}

	return EXIT_SUCCESS;
}
