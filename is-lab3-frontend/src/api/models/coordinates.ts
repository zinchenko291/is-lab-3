export interface CoordinatesDto {
  id: number;
  x: number;
  y: number;
}

export interface CoordinatesWithoutIdDto {
  x: number;
  y: number;
}

export type CoordinatesField = 'id' | 'x' | 'y';
