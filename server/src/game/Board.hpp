#pragma once

#include <array>
#include <string>
#include <map>


namespace game {

	class Board {
	public:
		static constexpr bool Is_Debug = true;
		static constexpr size_t kShip_Fields_Cnt = Is_Debug ? 2 : 20;

		static constexpr size_t kSize = 10;

		void Set_Ship(size_t row, size_t col);
		bool Is_Valid() const;

		bool Is_Guess(size_t row, size_t col) const;
		bool Is_Ship(size_t row, size_t col) const;

		bool Turn(size_t row, size_t col);

		static std::string Serialize_Field(size_t row, size_t col);
		static std::pair<size_t, size_t> Deserialize_Field(const std::string &field);

		bool Is_All_Ships_Guessed() const;

	private:
		static const std::map<size_t, size_t> kShips_Sizes_Cnts;

		std::array<bool, kSize * kSize> _ships;
		std::array<bool, kSize * kSize> _guesses;

		static size_t Get_Field_Idx(size_t row, size_t col);
	};

} // game
