import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CounterState {
  counter: number;
}

const initialState: CounterState = {
  counter: 0,
};

export const counterSlice = createSlice({
  name: 'counter',
  initialState,
  reducers: {
    setValue: (state: CounterState, newState: PayloadAction<number>) => {
      state.counter = newState.payload;
    },
  },
});

export const { setValue } = counterSlice.actions;
export const selectCount = (state: CounterState) => state.counter;
export default counterSlice.reducer;
