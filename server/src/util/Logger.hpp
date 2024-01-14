#pragma once

#include <string>
#include <memory>
#include <fstream>
#include <map>
#include <mutex>


namespace util {

	class Logger {
	public:
		enum class Log_Type {
			TRACE,
			INFO,
			ERROR
		};

		static void Trace(const std::string &text);
		static void Info(const std::string &text);
		static void Error(const std::string &text);

		explicit Logger(const std::string &path);
		void Log(Log_Type type, const std::string &text);
	private:
		static const std::map<Log_Type, const std::string> kLog_Type_String;

		static const std::string kPath;
		static std::unique_ptr<Logger> kInstance;

		static Logger &Get_Instance();

		mutable std::mutex _mutex;
		std::ofstream _ofstream;
	};

} // util
