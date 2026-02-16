import React, { useMemo, useState } from 'react';
import dayjs from 'dayjs';
import {
  Box,
  Button,
  Flex,
  HStack,
  Portal,
  Select,
  Text,
  VStack,
  createListCollection,
} from '@chakra-ui/react';
import { observer } from 'mobx-react-lite';

import type { VehicleField } from '../api/models/vehicles';
import type { SortDirection } from '../api/models/sortDirection';
import { useStores } from '../store';
import FilterValueControl from './FilterValueControl';

export type VehiclesFilterValues = {
  field?: VehicleField;
  value?: string;
  orderBy?: SortDirection;
};

type Props = {
  initialField?: VehicleField;
  initialValue?: string;
  initialOrderBy?: SortDirection;
};

type FieldKind = 'string' | 'number' | 'enum' | 'datetime';

export type FieldMeta = {
  value: string;
  label: string;
  kind: FieldKind;
  enumType?: 'type' | 'fuelType';
};

const fieldMeta: FieldMeta[] = [
  { value: 'id', label: 'ID', kind: 'number' },
  { value: 'name', label: 'Название', kind: 'string' },
  { value: 'creationDate', label: 'Дата создания', kind: 'datetime' },
  { value: 'enginePower', label: 'Мощность двигателя', kind: 'number' },
  { value: 'numberOfWheels', label: 'Количество колёс', kind: 'number' },
  { value: 'distanceTravelled', label: 'Пробег', kind: 'number' },
  { value: 'type', label: 'Тип ТС', kind: 'enum', enumType: 'type' },
  {
    value: 'fuelType',
    label: 'Тип топлива',
    kind: 'enum',
    enumType: 'fuelType',
  },
  { value: 'x', label: 'X', kind: 'number' },
  { value: 'y', label: 'Y', kind: 'number' },
];

const fieldCollection = createListCollection({
  items: fieldMeta.map((f) => ({
    label: f.label,
    value: f.value,
  })),
});

const orderByCollection = createListCollection({
  items: [
    { label: 'По возрастанию', value: 'asc' as SortDirection },
    { label: 'По убыванию', value: 'desc' as SortDirection },
  ],
});

const VehiclesFilter = observer(
  ({ initialField, initialValue, initialOrderBy }: Props) => {
    const { vehiclesStore } = useStores();

    const [field, setField] = useState<VehicleField | undefined>(initialField);
    const [value, setValue] = useState<string>(initialValue ?? '');
    const [orderBy, setOrderBy] = useState<SortDirection | undefined>(
      initialOrderBy,
    );

    const currentMeta = useMemo(
      () => fieldMeta.find((f) => f.value === field),
      [field],
    );

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();

      const meta = fieldMeta.find((f) => f.value === field);
      let finalValue: string | undefined = value || undefined;

      if (meta?.kind === 'datetime' && value) {
        const dt = dayjs(value);
        finalValue = dt.isValid() ? dt.toDate().toISOString() : undefined;
      }

      vehiclesStore.setFilters({
        field,
        value: finalValue,
        orderBy,
      });
    };

    const handleReset = () => {
      setField(undefined);
      setValue('');
      setOrderBy(undefined);
      vehiclesStore.setFilters({});
    };

    return (
      <Box
        as="form"
        onSubmit={handleSubmit}
        p={4}
        borderWidth="1px"
        borderRadius="md"
        mb={6}
      >
        <Text fontSize="lg" fontWeight="semibold" mb={3}>
          Фильтры
        </Text>

        <VStack align="stretch" gap={4}>
          <Flex gap={4} flexWrap="wrap">
            <Box minW="220px">
              <Select.Root
                collection={fieldCollection}
                size="sm"
                width="100%"
                value={field ? [field] : []}
                onValueChange={(details: any) => {
                  const v = details.value?.[0] as VehicleField | undefined;
                  setField(v);
                  setValue('');
                }}
              >
                <Select.HiddenSelect />
                <Select.Label>Поле</Select.Label>
                <Select.Control>
                  <Select.Trigger>
                    <Select.ValueText placeholder="Выберите поле" />
                  </Select.Trigger>
                  <Select.IndicatorGroup>
                    <Select.Indicator />
                  </Select.IndicatorGroup>
                </Select.Control>
                <Portal>
                  <Select.Positioner>
                    <Select.Content>
                      {fieldCollection.items.map((item) => (
                        <Select.Item item={item} key={item.value}>
                          {item.label}
                          <Select.ItemIndicator />
                        </Select.Item>
                      ))}
                    </Select.Content>
                  </Select.Positioner>
                </Portal>
              </Select.Root>
            </Box>

            <Box flex="1" minW="260px">
              <Text mb={1}>Значение</Text>
              <FilterValueControl
                meta={currentMeta}
                value={value}
                onChange={setValue}
              />
            </Box>

            <Box minW="220px">
              <Select.Root
                collection={orderByCollection}
                size="sm"
                width="100%"
                value={orderBy ? [orderBy] : []}
                onValueChange={(details: any) => {
                  const v = details.value?.[0] as SortDirection | undefined;
                  setOrderBy(v);
                }}
              >
                <Select.HiddenSelect />
                <Select.Label>Сортировка</Select.Label>
                <Select.Control>
                  <Select.Trigger>
                    <Select.ValueText placeholder="Без сортировки" />
                  </Select.Trigger>
                  <Select.IndicatorGroup>
                    <Select.Indicator />
                  </Select.IndicatorGroup>
                </Select.Control>
                <Portal>
                  <Select.Positioner>
                    <Select.Content>
                      {orderByCollection.items.map((item) => (
                        <Select.Item item={item} key={item.value}>
                          {item.label}
                          <Select.ItemIndicator />
                        </Select.Item>
                      ))}
                    </Select.Content>
                  </Select.Positioner>
                </Portal>
              </Select.Root>
            </Box>
          </Flex>

          <HStack justify="flex-end" gap={3}>
            <Button
              variant="outline"
              size="sm"
              type="button"
              onClick={handleReset}
            >
              Сбросить
            </Button>
            <Button colorScheme="blue" size="sm" type="submit">
              Применить
            </Button>
          </HStack>
        </VStack>
      </Box>
    );
  },
);

export default VehiclesFilter;
