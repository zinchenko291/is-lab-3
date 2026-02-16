import type { CoordinatesDto, CoordinatesWithoutIdDto } from './coordinates';

export interface VehicleDto {
  id: number;
  name: string;
  coordinates: CoordinatesDto;
  creationDate: string;
  enginePower: number;
  numberOfWheels: number;
  distanceTravelled: number;
  type: VehicleType | null;
  fuelType: FuelType | null;
  capacity: number;
  fuelConsumption: number;
  owner: {
    id: number;
    name: string;
  }
}

export interface VehicleWithoutIdDto {
  name: string;
  coordinates: CoordinatesWithoutIdDto;
  enginePower: number;
  numberOfWheels: number;
  distanceTravelled: number;
  type: VehicleType | null;
  fuelType: FuelType | null;
  capacity: number;
  fuelConsumption: number;
  owner: {
    id: number;
    name: string;
  }
}

export type VehicleField =
  | 'id'
  | 'name'
  | 'creationDate'
  | 'enginePower'
  | 'numberOfWheels'
  | 'distanceTravelled'
  | 'type'
  | 'fuelType';

export enum VehicleType {
  BOAT = 'BOAT',
  SHIP = 'SHIP',
  MOTORCYCLE = 'MOTORCYCLE',
  CHOPPER = 'CHOPPER',
}

export enum FuelType {
  KEROSENE = 'KEROSENE',
  ALCOHOL = 'ALCOHOL',
  MANPOWER = 'MANPOWER',
  PLASMA = 'PLASMA',
  ANTIMATTER = 'ANTIMATTER',
}
