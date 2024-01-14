#include "Logger.hpp"

#include <iomanip>
#include <thread>


namespace util {

	const std::map<Logger::Log_Type, const std::string> Logger::kLog_Type_String{
			{Log_Type::TRACE, "TRACE"},
			{Log_Type::INFO, "INFO"},
			{Log_Type::ERROR, "ERROR"}
	};

	std::unique_ptr<Logger> Logger::kInstance = nullptr;

	const std::string Logger::kPath = "log.txt";

	Logger::Logger(const std::string &path) : _ofstream(path) {
		//
	}

	void Logger::Log(Logger::Log_Type type, const std::string &text) {
		const auto time = std::time(nullptr);
		const auto *time_info = std::localtime(&time);

		std::lock_guard lck{_mutex};
		_ofstream << std::put_time(time_info, "%Y-%m-%d %H:%M:%S") << " | ";
		_ofstream << "Thread " << std::this_thread::get_id() << " | ";
		_ofstream << kLog_Type_String.at(type) << " | ";
		_ofstream << text << std::endl;
	}

	void Logger::Trace(const std::string &text) {
		Get_Instance().Log(Log_Type::TRACE, text);
	}

	void Logger::Info(const std::string &text) {
		Get_Instance().Log(Log_Type::INFO, text);
	}

	void Logger::Error(const std::string &text) {
		Get_Instance().Log(Log_Type::ERROR, text);
	}

	Logger &Logger::Get_Instance() {
		if (!kInstance) {
			kInstance = std::make_unique<Logger>(kPath);
		}
		return *kInstance;
	}

} // util