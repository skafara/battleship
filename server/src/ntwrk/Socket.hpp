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

		std::byte Read_Byte() const;
		void Write_Byte(std::byte byte) const;

	private:
		int _fd;
	};

} // ntwrk
