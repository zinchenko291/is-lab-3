import type { FuelType, VehicleType } from '../../api/models/vehicles';

export type CoordinateMode = 'none' | 'new' | 'existing';

export type FormState = {
  name: string;
  type: VehicleType | '';
  enginePower: string;
  numberOfWheels: string;
  capacity: string;
  distanceTravelled: string;
  fuelConsumption: string;
  fuelType: FuelType | '';
  coordX: string;
  coordY: string;
};

export type FormErrors = {
  name?: string;
  enginePower?: string;
  numberOfWheels?: string;
  capacity?: string;
  distanceTravelled?: string;
  fuelConsumption?: string;
  fuelType?: string;
  coordX?: string;
  coordY?: string;
  coordMode?: string;
  coordChoice?: string;
  common?: string;
};
