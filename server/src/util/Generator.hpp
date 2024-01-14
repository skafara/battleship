#pragma once

#include <random>


namespace util {

	/// Generator Util
	class Generator {
	public:
		/// Generates a random number from interval <a,b>
		/// \param a a
		/// \param b b
		/// \return Random number
		static size_t From_Range(size_t a, size_t b);
	};

} // util
