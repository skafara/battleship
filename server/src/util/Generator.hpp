#pragma once

#include <random>


namespace util {

	class Generator {
	public:
		static size_t From_Range(size_t a, size_t b);

	private:
		static const std::random_device _rd;
		static std::mt19937 _generator;
	};

} // util
