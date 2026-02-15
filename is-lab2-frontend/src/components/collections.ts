import { createListCollection } from '@chakra-ui/react';

export const fuelTypeCollection = createListCollection({
  items: [
    { label: 'KEROSENE', value: 'KEROSENE' },
    { label: 'ALCOHOL', value: 'ALCOHOL' },
    { label: 'MANPOWER', value: 'MANPOWER' },
    { label: 'PLASMA', value: 'PLASMA' },
    { label: 'ANTIMATTER', value: 'ANTIMATTER' },
  ],
});

export const vehicleTypeCollection = createListCollection({
  items: [
    { label: 'BOAT', value: 'BOAT' },
    { label: 'SHIP', value: 'SHIP' },
    { label: 'MOTORCYCLE', value: 'MOTORCYCLE' },
    { label: 'CHOPPER', value: 'CHOPPER' },
  ],
});
