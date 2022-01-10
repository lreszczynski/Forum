import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface HistoryState {
  location: String;
}

const initialState: HistoryState = {
  location: '/',
};

export const historySlice = createSlice({
  name: 'history',
  initialState,
  reducers: {
    setLocation: (state: HistoryState, newLocation: PayloadAction<string>) => {
      state.location = newLocation.payload;
    },
  },
});

// Action creators are generated for each case reducer function
export const { setLocation } = historySlice.actions;
// Other code such as selectors can use the imported `RootState` type
export default historySlice.reducer;
