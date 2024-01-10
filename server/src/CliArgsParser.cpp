#include "CliArgsParser.hpp"

#include <regex>
#include <limits>
#include <array>


CliArgsParser::Result::Result(const std::string &ip, uint16_t port, size_t lim_clients, size_t lim_rooms)
		: ip(ip), port(port), lim_clients(lim_clients), lim_rooms(lim_rooms) {
	//
}

void CliArgsParser::Ignore_Opt_Errs() {
	opterr = 0;
}

bool CliArgsParser::Is_Help(int argc, char **argv) {
	for (int opt; (opt = getopt_long(argc, argv, "h", kOptions, nullptr)) != -1; ) {
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
		for (int opt; (opt = getopt_long(argc, argv, "i:p:c:r:", kOptions, nullptr)) != -1; ) {
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
				default:
					break;
			}
		}
	}
	catch (const std::invalid_argument &e) {
		Reset_Opt_Ind();
		throw e;
	}

	Reset_Opt_Ind();
	if (!std::ranges::all_of(parsed, [](const bool item) { return item; })) {
		throw std::invalid_argument{"Missing required options"};
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

	const std::regex rx_ip{"^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"};
	if (std::regex_match(str, rx_ip)) {
		return str;
	}

	throw std::invalid_argument{"Invalid IP address '" + str + "'"};
}

uint16_t CliArgsParser::Parse_Port(const std::string &str) {
	const long long num = std::stoll(str);
	if (num < 0 || num > std::numeric_limits<uint16_t>::max()) {
		throw std::invalid_argument{"Invalid port '" + str + "'"};
	}

	return static_cast<uint16_t>(num);
}

size_t CliArgsParser::Parse_Lim_Clients(const std::string &str) {
	const long long num = std::stoll(str);
	if (num < 0 || num > std::numeric_limits<size_t>::max()) {
		throw std::invalid_argument{"Invalid clients count limit '" + str + "'"};
	}

	return num;
}

size_t CliArgsParser::Parse_Lim_Rooms(const std::string &str) {
	const long long num = std::stoll(str);
	if (num < 0 || num > std::numeric_limits<size_t>::max()) {
		throw std::invalid_argument{"Invalid rooms count limit '" + str + "'"};
	}

	return num;
}
