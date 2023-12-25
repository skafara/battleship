#pragma once

#include <cstddef>
#include <cstdint>


namespace ntwrk {

	class Socket {
	public:
		explicit Socket(int fd);

		Socket(const Socket &other) = delete;
		Socket &operator=(const Socket &other) = delete;

		Socket(Socket &&other) noexcept;
		Socket &operator=(Socket &&other) noexcept;

		~Socket();

		//size_t Has_Bytes();

		//void Read_Bytes(uint8_t &buf, size_t cnt);
		//void Write_Bytes(const uint8_t &buf, size_t cnt);

	private:
		int _fd;
	};

} // ntwrk
