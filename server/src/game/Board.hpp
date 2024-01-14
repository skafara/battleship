#pragma once

#include <array>
#include <string>
#include <map>


namespace game {

	/// Board
	class Board {
	public:
		static constexpr bool kIs_Debug = false;
		/// Ship Fields Count (4x1, 3x2, 2x3, 1x4)
		static constexpr size_t kShip_Fields_Cnt = kIs_Debug ? 2 : 20;

		/// Board Size
		static constexpr size_t kSize = 10;

		/// Sets ship (row, col)
		/// \param row Row
		/// \param col Col
		void Set_Ship(size_t row, size_t col);
		/// Returns whether a board is valid
		/// \return Bool
		bool Is_Valid() const;

		/// Returns whether a (row, col) has been previously guessed
		/// \param row Row
		/// \param col Col
		/// \return Bool
		bool Is_Guess(size_t row, size_t col) const;
		/// Returns whether a (row, col) is a ship
		/// \param row Row
		/// \param col Col
		/// \return Bool
		bool Is_Ship(size_t row, size_t col) const;
		/// Returns whether a (row, col) has been previously invalidated
		/// (Invalidated fields are not-guessed fields left after sinking a ship)
		/// \param row Row
		/// \param col Col
		/// \return Bool
		bool Is_Invalidated(size_t row, size_t col) const;
		/// Returns whether all ships have been guessed
		/// \return Bool
		bool Is_All_Ships_Guessed() const;

		/// Returns invalidated fields in the latest turn
		/// \return Invalidated fields
		const std::vector<std::pair<size_t, size_t>> &Get_Latest_Invalidated() const;

		/// Performs a turn on (row, col)
		/// If it is hit and ship is sunk, latest invalidated fields are set
		/// \param row Row
		/// \param col Col
		/// \return True if hit, else false
		bool Turn(size_t row, size_t col);

		/// Serializes (row, col)
		/// \param row Row
		/// \param col Col
		/// \return Field string representation
		static std::string Serialize_Field(size_t row, size_t col);
		/// Deserializes a field string representation
		/// \param field Field string representation
		/// \return (row, col)
		static std::pair<size_t, size_t> Deserialize_Field(const std::string &field);

	private:
		static const std::map<size_t, size_t> kShips_Sizes_Cnts;

		std::array<bool, kSize * kSize> _ships;
		std::array<bool, kSize * kSize> _invalidated;
		std::array<bool, kSize * kSize> _guesses;

		std::vector<std::pair<size_t, size_t>> _latest_invalidated;

		static size_t Get_Field_Idx(size_t row, size_t col);
	};

} // game
