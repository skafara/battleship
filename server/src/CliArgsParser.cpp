#include "CliArgsParser.hpp"

#include <regex>
#include <limits>


CliArgsParser::Result::Result(const std::string &ip, uint16_t port, size_t lim_clients, size_t lim_rooms)
		: ip(ip), port(port), lim_clients(lim_clients), lim_rooms(lim_rooms) {
	//
}

void CliArgsParser::Ignore_Opt_Errs() {
	opterr = 0;
}

bool CliArgsParser::Is_Help(int argc, char **argv) {
	int opt{};
	while ((opt = getopt_long(argc, argv, "h", kOptions, nullptr)) != -1) {
		if (opt == 'h') {
			Reset_Opt_Ind();
			return true;
		}
	}

	Reset_Opt_Ind();
	return false;
}

CliArgsParser::Result CliArgsParser::Parse(int argc, char **argv) {
	std::string ip{};
	uint16_t port{};
	size_t lim_clients{};
	size_t lim_rooms{};

	std::array<bool, kExpected_Args_Cnt> parsed{};
	try {
		int opt;
		while ((opt = getopt_long(argc, argv, "i:p:c:r:", kOptions, nullptr)) != -1) {
			switch (opt) {
				case 'i':
					ip = Parse_IP(optarg);
					parsed[0] = true;
					break;
				case 'p':
					port = Parse_Port(optarg);
					parsed[1] = true;
					break;
				case 'c':
					lim_clients = Parse_Lim_Clients(optarg);
					parsed[2] = true;
					break;
				case 'r':
					lim_rooms = Parse_Lim_Rooms(optarg);
					parsed[3] = true;
					break;
			}
		}
	}
	catch (int i) {
		Reset_Opt_Ind();
		throw i;
	}

	Reset_Opt_Ind();
	if (!std::ranges::all_of(parsed, [](const bool item) { return item; })) {
		throw -1;
	}
	return {ip, port, lim_clients, lim_rooms};
}

void CliArgsParser::Reset_Opt_Ind() {
	optind = kOpt_Ind_Begin;
}

std::string CliArgsParser::Parse_IP(const std::string &str) {
	if (str == "localhost") {
		return str;
	}

	const std::regex rx_local_ip{
			R"(\b(127\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|0?10\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|172\.0?1[6-9]\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|172\.0?2[0-9]\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|172\.0?3[01]\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|192\.168\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|169\.254\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|::1|[fF][cCdD][0-9a-fA-F]{2}(?:[:][0-9a-fA-F]{0,4}){0,7}|[fF][eE][89aAbB][0-9a-fA-F](?:[:][0-9a-fA-F]{0,4}){0,7})(?:\/([789]|1?[0-9]{2}))?\b)"};
	if (std::regex_match(str, rx_local_ip)) {
		return str;
	}

	throw -1;
}

uint16_t CliArgsParser::Parse_Port(const std::string &str) {
	const int num = std::stoi(str);
	if (num < 0 || num > std::numeric_limits<uint16_t>::max()) {
		throw -1;
	}

	return static_cast<uint16_t>(num);
}

size_t CliArgsParser::Parse_Lim_Clients(const std::string &str) {
	return std::stoull(str); // TODO
}

size_t CliArgsParser::Parse_Lim_Rooms(const std::string &str) {
	return std::stoull(str); // TODO
}
