#pragma once

#include <cstddef>
#include <cstdint>
#include <stdexcept>
#include <string>


namespace ntwrk {

	/// Socket Exception
	class SocketException : public std::runtime_error {
	public:
		/// Transparently constructs
		/// \param text Text
		explicit SocketException(const std::string &text) : std::runtime_error(text) {
			//
		}
	};

	/// Socket
	class Socket {
	public:
		/// Wraps given file descriptor
		/// \throws SocketException
		/// \param fd File descriptor
		explicit Socket(int fd);

		Socket(const Socket &other) = delete;
		Socket &operator=(const Socket &other) = delete;

		Socket(Socket &&other) noexcept;
		Socket &operator=(Socket &&other) noexcept;

		~Socket();

		/// Reads a byte (blocks)
		/// \throws SocketException if socket is closed by the other side or if there is any other problem
		/// \return Byte
		std::byte Read_Byte() const;
		/// Writes a byte (does not block)
		/// Does not throw if socket is closed by the other side or if there is any other problem
		/// \param byte
		void Write_Byte(std::byte byte) const;

	private:
		int _fd;
	};

} // ntwrk
