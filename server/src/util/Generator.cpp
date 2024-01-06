#include "Generator.hpp"

#include <random>


namespace util {

	size_t Generator::From_Range(size_t a, size_t b) {
		//std::uniform_int_distribution<size_t> distribution(a, b);
		//return distribution(_generator);
		return a + std::rand() % (b - a + 1);
	}

} // util