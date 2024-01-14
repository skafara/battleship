#pragma once

#include <string>
#include <getopt.h>
#include <regex>


/// CLI Arguments Parser
class CliArgsParser {
public:
	/// Parse Result
	class Result {
	public:
		const std::string ip; /// IP Address
		const uint16_t port; /// Port
		const size_t lim_clients; /// Clients Count Limit
		const size_t lim_rooms; /// Rooms Count Limit

		/// Transparently constructs
		Result(const std::string &ip, uint16_t port, size_t lim_clients, size_t lim_rooms);
	};

	/// Ignore getopt error outputs
	static void Ignore_Opt_Errs();

	/// Returns whether args contain option for help
	/// \param argc argc
	/// \param argv argv
	/// \return Bool
	static bool Is_Help(int argc, char **argv);
	static Result Parse(int argc, char **argv);

private:
	static constexpr option kOptions[] = {
			{"help", no_argument, nullptr, 'h'},
			{"ip", required_argument, nullptr, 'i'},
			{"port", required_argument, nullptr, 'p'},
			{"lim-clients", required_argument, nullptr, 'c'},
			{"lim-rooms", required_argument, nullptr, 'r'},
			nullptr
	};

	static constexpr size_t kExpected_Args_Cnt = 4;

	static constexpr int kOpt_Ind_Begin = 1;
	static void Reset_Opt_Ind();

	static std::string Parse_IP(const std::string &str);
	static uint16_t Parse_Port(const std::string &str);
	static size_t Parse_Lim_Clients(const std::string &str);
	static size_t Parse_Lim_Rooms(const std::string &str);
};
