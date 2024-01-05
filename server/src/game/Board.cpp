#include "Board.hpp"

#include <sstream>


namespace game {

	void Board::Set_Ship(size_t row, size_t col) {
		_ships[Get_Field_Idx(row, col)] = true;
	}

	bool Board::Is_Valid() const {
		return true;
	}

	bool Board::Is_Guess(size_t row, size_t col) const {
		return _guesses[Get_Field_Idx(row, col)];
	}

	bool Board::Turn(size_t row, size_t col) {
		const size_t field_idx = Get_Field_Idx(row, col);
		const bool is_ship = _ships[field_idx];

		_guesses[Get_Field_Idx(row, col)] = true;
		return is_ship;
	}

	size_t Board::Get_Field_Idx(size_t row, size_t col) {
		return 10 * row + col;
	}

	std::string Board::Serialize_Field(size_t row, size_t col) {
		std::ostringstream osstream;
		osstream << (row + '0');
		osstream << (col + '0');
		return osstream.str();
	}

	std::pair<size_t, size_t> Board::Deserialize_Field(const std::string &field) {
		if (field.length() != 2) {
			throw std::invalid_argument{"bbbbbbb"};
		}

		const size_t row = field[0] - '0';
		const size_t col = field[1] - '0';
		if (row >= 10 || col >= 10) {
			throw std::invalid_argument{"bbbbbbb"};
		}

		return {row, col};
	}

	bool Board::Is_All_Ships_Guessed() const {
		return false; // TODO
	}

} // game