#include "Message.hpp"

#include <sstream>
#include <regex>

#include "iostream"
namespace msgs {

	MessageType Message::Get_Type() const {
		return _type;
	}

	const std::string &Message::Get_Param(size_t idx) const {
		return _params[idx];
	}

	std::string Message::Serialize() const  {
		std::ostringstream osstream;

		osstream << kMessageType_String.at(_type);
		for (const std::string &param : _params) {
			osstream << kParam_Delimiter;
			for (char c : param) {
				if (c == kEscape_Char || c == kParam_Delimiter) {
					osstream << kEscape_Char;
				}
				osstream << c;
			}
		}

		return osstream.str();
	}

	Message Message::Deserialize(const std::string &str) {
		std::istringstream isstream{str};
		std::cout << "Deserialize: " << str << std::endl;

		std::string type_str;
		std::getline(isstream, type_str, kParam_Delimiter);

		const auto it_msg_type = std::ranges::find_if(kMessageType_String, [&type_str](const auto &pair) -> bool {
			return pair.second == type_str;
		});
		if (it_msg_type == std::end(kMessageType_String)) {
			throw IllegalMessageException{"Unknown Message Type"};
		}

		const MessageType type = (*it_msg_type).first;
		if (!kMessageType_Params_Cnt.contains(type)) {
			throw IllegalMessageException{"Unexpected Message Type"};
		}

		Message msg{type};

		if (str.length() == type_str.length()) {
			if (msg._params.size() != kMessageType_Params_Cnt.at(msg.Get_Type())) {
				throw IllegalMessageException{"Invalid Message Parameters Count"};
			}

			return msg;
		}

		bool escape = false;
		std::ostringstream param_osstream;
		for (size_t i = type_str.length() + 1; ; ++i) {
			if (i >= str.length()) {
				msg.Store_Param(param_osstream.str());
				break;
			}

			const char c = str[i];
			if (!escape) {
				if (c == kEscape_Char) {
					escape = true;
					continue;
				}

				if (c == kParam_Delimiter) {
					msg.Store_Param(param_osstream.str());
					param_osstream = std::ostringstream{};
					continue;
				}
			}

			escape = false;
			param_osstream << c;
		}

		if (msg._params.size() != kMessageType_Params_Cnt.at(msg.Get_Type())) {
			throw IllegalMessageException{"Invalid Message Parameters Count"};
		}

		return msg;
	}

} // msgs