export function screenWidth() {
  return (
    window.innerWidth ||
    document.documentElement.clientWidth ||
    document.body.clientWidth
  );
}

export const breakpoints = {
  xs: 512,
  sm: 768,
  md: 896,
  lg: 1152,
  xl: 1280,
};

export function isScreenWidthLessThan(value: number) {
  return screenWidth() < value;
}
