#pragma once

#include <array>


namespace game {

	class Board {
	public:
		void Set_Ship(size_t row, size_t col);
		bool Is_Valid() const;

		bool Is_Guess(size_t row, size_t col) const;

		//bool Is_Turn_Valid(size_t row, size_t col) const;
		bool Turn(size_t row, size_t col);

		static std::string Serialize_Field(size_t row, size_t col);
		static std::pair<size_t, size_t> Deserialize_Field(const std::string &field);

		bool Is_All_Ships_Guessed() const;

	private:
		std::array<bool, 100> _ships;
		std::array<bool, 100> _guesses;

		static size_t Get_Field_Idx(size_t row, size_t col);
	};

} // game
