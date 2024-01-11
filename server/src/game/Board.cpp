#include "Board.hpp"

#include <sstream>
#include <map>
#include <algorithm>


namespace game {

	void Board::Set_Ship(size_t row, size_t col) {
		_ships[Get_Field_Idx(row, col)] = true;
	}

	bool Board::Is_Valid() const {
		std::map<size_t, size_t> ships_sizes_cnts{{1, 4}, {2, 3}, {3, 2}, {4, 1}};
		//std::map<size_t, size_t> ships_sizes_cnts{{1, 2}};
		std::array<bool, kSize * kSize> visited{};

		const auto Process_Ship = [this, &visited, &ships_sizes_cnts](size_t row, size_t col) -> bool {
			// Check corners for diagonal overlapping
			if (row != 0 && col != 0 && _ships[Get_Field_Idx(row - 1, col - 1)]) return false;
			if (row != 0 && col + 1 < kSize && _ships[Get_Field_Idx(row - 1, col + 1)]) return false;
			if (row + 1 < kSize && col != 0 && _ships[Get_Field_Idx(row + 1, col - 1)]) return false;
			if (row + 1 < kSize && col + 1 < kSize && _ships[Get_Field_Idx(row + 1, col + 1)]) return false;

			size_t off_col, off_row;
			// Check horizontal direction for ship continuation
			for (off_col = 1; ; ++off_col) {
				if (col + off_col >= kSize || !_ships[Get_Field_Idx(row, col + off_col)]) break;

				// Check vertical direction for overlapping
				if (row + 1 < kSize && _ships[Get_Field_Idx(row + 1, col + off_col)]) return false;
				visited[Get_Field_Idx(row, col + off_col)] = true;
			}
			// Check vertical direction for ship continuation
			for (off_row = 1; ; ++off_row) {
				if (row + off_row >= kSize || !_ships[Get_Field_Idx(row + off_row, col)]) break;

				// Check horizontal direction for overlapping
				if (col + 1 < kSize && _ships[Get_Field_Idx(row + off_row, col + 1)]) return false;
				visited[Get_Field_Idx(row + off_row, col)] = true;
			}

			if (off_col > 1 && off_row > 1) {
				return false;
			}

			const size_t ship_size = std::max(off_col, off_row);
			if (!ships_sizes_cnts.contains(ship_size)) {
				return false;
			}

			ships_sizes_cnts[ship_size]--;
			return true;
		};

		for (size_t row = 0; row < kSize; ++row) {
			for (size_t col = 0; col < kSize; ++col) {
				if (std::ranges::all_of(ships_sizes_cnts, [](const auto &pair) {return pair.second == 0;}) &&
					!visited[Get_Field_Idx(row, col)] &&
					_ships[Get_Field_Idx(row, col)]) {
					return false;
				}
				if (!visited[Get_Field_Idx(row, col)] && _ships[Get_Field_Idx(row, col)]) {
					if (!Process_Ship(row, col)) {
						return false;
					}
				}
			}
		}

		return std::ranges::all_of(ships_sizes_cnts, [](const auto &pair) {return pair.second == 0;});
	}

	bool Board::Is_Guess(size_t row, size_t col) const {
		return _guesses[Get_Field_Idx(row, col)];
	}

	bool Board::Is_Ship(size_t row, size_t col) const {
		return _ships[Get_Field_Idx(row, col)];
	}

	bool Board::Turn(size_t row, size_t col) {
		const size_t field_idx = Get_Field_Idx(row, col);
		const bool is_ship = _ships[field_idx];

		_guesses[field_idx] = true;
		return is_ship;
	}

	size_t Board::Get_Field_Idx(size_t row, size_t col) {
		return kSize * row + col;
	}

	std::string Board::Serialize_Field(size_t row, size_t col) {
		std::ostringstream osstream;
		osstream << static_cast<char>(row + '0');
		osstream << static_cast<char>(col + '0');
		return osstream.str();
	}

	std::pair<size_t, size_t> Board::Deserialize_Field(const std::string &field) {
		if (field.length() != 2) {
			throw std::invalid_argument{"Invalid Field Position Length"};
		}

		const size_t row = field[0] - '0';
		const size_t col = field[1] - '0';
		if (row >= kSize || col >= kSize) {
			throw std::invalid_argument{"Invalid Field Position"};
		}

		return {row, col};
	}

	bool Board::Is_All_Ships_Guessed() const {
		for (size_t i = 0; i < _guesses.size(); ++i) {
			if (!_guesses[i] && _ships[i]) {
				return false;
			}
		}
		return true;
	}

} // game