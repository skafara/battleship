#pragma once

#include <string>
#include <memory>
#include <fstream>
#include <map>
#include <mutex>


namespace util {

	/// Logger
	class Logger {
	public:
		/// Message Type
		enum class Msg_Type {
			TRACE,
			INFO,
			ERROR
		};

		/// Logs trace using default logger
		/// \param msg Message
		static void Trace(const std::string &msg);
		/// Logs info using default logger
		/// \param msg Message
		static void Info(const std::string &msg);
		/// Logs error using default logger
		/// \param msg Message
		static void Error(const std::string &msg);

		/// Constructs a file-outputting logger
		/// \param path File path
		explicit Logger(const std::string &path);
		/// Logs a message
		/// \param type Message Type
		/// \param msg Message
		void Log(Msg_Type type, const std::string &msg);
	private:
		static const std::map<Msg_Type, const std::string> kLog_Type_String;

		static const std::string kPath;
		static std::unique_ptr<Logger> kInstance;

		static Logger &Get_Instance();

		mutable std::mutex _mutex;
		std::ofstream _ofstream;
	};

} // util
