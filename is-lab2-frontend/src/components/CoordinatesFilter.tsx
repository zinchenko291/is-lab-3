import { useMemo, useState } from 'react';
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
import type { CoordinatesField } from '../api/models/coordinates';
import type { SortDirection } from '../api/models/sortDirection';
import { observer } from 'mobx-react-lite';
import { useStores } from '../store';
import FilterValueControl from './FilterValueControl';

export type CoordinatesFilterValues = {
  field?: CoordinatesField;
  value?: string;
  orderBy?: SortDirection;
};

type Props = {
  initialField?: CoordinatesField;
  initialValue?: string;
  initialOrderBy?: SortDirection;
};

type FieldKind = 'number';

const fieldMeta: {
  value: CoordinatesField;
  label: string;
  kind: FieldKind;
}[] = [
  { value: 'id' as CoordinatesField, label: 'ID', kind: 'number' },
  { value: 'x' as CoordinatesField, label: 'X', kind: 'number' },
  { value: 'y' as CoordinatesField, label: 'Y', kind: 'number' },
];

const fieldCollection = createListCollection({
  items: fieldMeta.map((f) => ({
    label: f.label,
    value: f.value,
  })),
});

const orderByCollection = createListCollection({
  items: [
    { label: 'По возрастанию', value: 'asc' },
    { label: 'По убыванию', value: 'desc' },
  ],
});

const CoordinatesFilter = observer(({
  initialField,
  initialValue,
  initialOrderBy,
}: Props) => {
  const { coordinatesStore } = useStores();
  const [field, setField] = useState<CoordinatesField | undefined>(
    initialField
  );
  const [value, setValue] = useState<string>(initialValue ?? '');
  const [orderBy, setOrderBy] = useState<SortDirection | undefined>(
    initialOrderBy
  );

  const currentMeta = useMemo(
    () => fieldMeta.find((f) => f.value === field),
    [field]
  );

  const handleApply = () => {
    coordinatesStore.setFilters({
      field,
      value: value || undefined,
      orderBy,
    });
  };

  const handleReset = () => {
    setField(undefined);
    setValue('');
    setOrderBy(undefined);
    coordinatesStore.setFilters({});
  };

  return (
    <Box p={4} borderWidth="1px" borderRadius="md" mb={6}>
      <Text fontSize="lg" fontWeight="semibold" mb={3}>
        Фильтры координат
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
                const v = details.value?.[0] as CoordinatesField | undefined;
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
            <FilterValueControl value={value} onChange={setValue} meta={currentMeta} />
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
          <Button variant="outline" size="sm" onClick={handleReset}>
            Сбросить
          </Button>
          <Button colorScheme="blue" size="sm" onClick={handleApply}>
            Применить
          </Button>
        </HStack>
      </VStack>
    </Box>
  );
});

export default CoordinatesFilter;
