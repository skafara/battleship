#include "Board.hpp"

#include <sstream>
#include <map>
#include <algorithm>


namespace game {

	const std::map<size_t, size_t> Board::kShips_Sizes_Cnts = kIs_Debug ?
			std::map<size_t, size_t>{{1, 2}} : std::map<size_t, size_t>{{1, 4}, {2, 3}, {3, 2}, {4, 1}};

	void Board::Set_Ship(size_t row, size_t col) {
		_ships[Get_Field_Idx(row, col)] = true;
	}

	bool Board::Is_Valid() const {
		std::array<bool, kSize * kSize> visited{};
		std::map<size_t, size_t> ships_sizes_cnts = kShips_Sizes_Cnts;

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

	bool Board::Is_Invalidated(size_t row, size_t col) const {
		return _invalidated[Get_Field_Idx(row, col)];
	}

	bool Board::Is_All_Ships_Guessed() const {
		for (size_t i = 0; i < _guesses.size(); ++i) {
			if (!_guesses[i] && _ships[i]) {
				return false;
			}
		}
		return true;
	}

	const std::vector<std::pair<size_t, size_t>> &Board::Get_Latest_Invalidated() const {
		return _latest_invalidated;
	}

	bool Board::Turn(size_t row, size_t col) {
		_latest_invalidated.clear();

		const size_t field_idx = Get_Field_Idx(row, col);
		const bool is_ship = _ships[field_idx];
		_guesses[field_idx] = true;

		if (is_ship) {
			size_t col_r, col_l, row_t, row_b;
			bool all_guessed = true;
			for (col_r = col; col_r + 1 < kSize && Is_Ship(row, col_r + 1); ++col_r) if (!Is_Guess(row, col_r + 1)) all_guessed = false;
			for (col_l = col; col_l != 0 && Is_Ship(row, col_l - 1); --col_l) if (!Is_Guess(row, col_l - 1)) all_guessed = false;
			for (row_b = row; row_b + 1 < kSize && Is_Ship(row_b + 1, col); ++row_b) if (!Is_Guess(row_b + 1, col)) all_guessed = false;
			for (row_t = row; row_t != 0 && Is_Ship(row_t - 1, col); --row_t) if (!Is_Guess(row_t - 1, col)) all_guessed = false;

			if (all_guessed) {
				for (size_t row_ = row_t; row_ <= row_b; ++row_) {
					for (size_t col_ = col_l; col_ <= col_r; ++col_) {
						for (int row_off = -1; row_off <= +1; ++row_off) {
							for (int col_off = -1; col_off <= +1; ++col_off) {
								if ((row_ == 0 && row_off == -1) || (row_ + row_off >= kSize)) continue;
								if ((col_ == 0 && col_off == -1) || (col_ + col_off >= kSize)) continue;

								const size_t pos_row_off = row_ + row_off;
								const size_t pos_col_off = col_ + col_off;
								if (!Is_Ship(pos_row_off, pos_col_off) && !Is_Guess(pos_row_off, pos_col_off) && !Is_Invalidated(pos_row_off, pos_col_off)) {
									_invalidated[Get_Field_Idx(pos_row_off, pos_col_off)] = true;
									_latest_invalidated.emplace_back(pos_row_off, pos_col_off);
								}}}}}
			}
		}

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

} // game
