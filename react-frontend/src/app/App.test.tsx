import { act, render, screen } from '@testing-library/react';
import React from 'react';
import Main from 'components/main/Main';

test('renders learn react link', async () => {
  await act(async () => {
    render(<Main />);
  });
  const linkElement = screen.getAllByText(/Lorem/i)[0];
  expect(linkElement).toBeInTheDocument();
});
