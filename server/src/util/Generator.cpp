#include "Generator.hpp"

#include <random>


namespace util {

	size_t Generator::From_Range(size_t a, size_t b) {
		return a + std::rand() % (b - a + 1);
	}

} // util