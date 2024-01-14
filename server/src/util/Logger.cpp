#include "Logger.hpp"

#include <iomanip>
#include <thread>


namespace util {

	const std::map<Logger::Msg_Type, const std::string> Logger::kLog_Type_String{
			{Msg_Type::TRACE, "TRACE"},
			{Msg_Type::INFO,  "INFO"},
			{Msg_Type::ERROR, "ERROR"}
	};

	std::unique_ptr<Logger> Logger::kInstance = nullptr;

	const std::string Logger::kPath = "log.txt";

	Logger::Logger(const std::string &path) : _ofstream(path) {
		//
	}

	void Logger::Log(Logger::Msg_Type type, const std::string &msg) {
		const auto time = std::time(nullptr);
		const auto *time_info = std::localtime(&time);

		std::lock_guard lck{_mutex};
		_ofstream << std::put_time(time_info, "%Y-%m-%d %H:%M:%S") << " | ";
		_ofstream << "Thread " << std::this_thread::get_id() << " | ";
		_ofstream << kLog_Type_String.at(type) << " | ";
		_ofstream << msg << std::endl;
	}

	void Logger::Trace(const std::string &msg) {
		Get_Instance().Log(Msg_Type::TRACE, msg);
	}

	void Logger::Info(const std::string &msg) {
		Get_Instance().Log(Msg_Type::INFO, msg);
	}

	void Logger::Error(const std::string &msg) {
		Get_Instance().Log(Msg_Type::ERROR, msg);
	}

	Logger &Logger::Get_Instance() {
		if (!kInstance) {
			kInstance = std::make_unique<Logger>(kPath);
		}
		return *kInstance;
	}

} // util