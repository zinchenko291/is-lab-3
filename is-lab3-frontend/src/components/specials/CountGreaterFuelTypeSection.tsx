import { useState } from 'react';
import {
  Box,
  Button,
  Flex,
  Portal,
  Select,
  Spinner,
  Text,
} from '@chakra-ui/react';
import { Fuel } from 'lucide-react';
import type { FuelType } from '../../api/models/vehicles';
import { fuelTypeCollection } from '../collections';
import { useStores } from '../../store';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const CountGreaterFuelTypeSection = () => {
  const { vehiclesStore } = useStores();
  const [fuelType, setFuelType] = useState<FuelType | ''>('');
  const [count, setCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCount = async () => {
    if (!fuelType) {
      setError('Выберите тип топлива');
      return;
    }
    try {
      setLoading(true);
      setError(null);
      const result = await vehiclesStore.countGreaterFuel(fuelType);
      setCount(result);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      setError(message ?? 'Не удалось выполнить запрос');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box borderWidth="1px" borderRadius="md" p={4}>
      <Flex align="center" gap={2} mb={3}>
        <Fuel size={18} />
        <Text fontSize="lg" fontWeight="semibold">
          Кол-во ТС с типом топлива больше заданного
        </Text>
      </Flex>

      <Flex gap={4} align="flex-end" flexWrap="wrap">
        <Box minW="220px">
          <Text mb={1}>Тип топлива (нижняя граница)</Text>
          <Select.Root
            collection={fuelTypeCollection}
            size="sm"
            width="100%"
            value={fuelType ? [fuelType] : []}
            onValueChange={(details: any) => {
              const v = details.value?.[0] as FuelType | undefined;
              setFuelType(v ?? '');
              setError(null);
            }}
          >
            <Select.HiddenSelect />
            <Select.Control>
              <Select.Trigger>
                <Select.ValueText placeholder="Выберите тип топлива" />
              </Select.Trigger>
              <Select.IndicatorGroup>
                <Select.Indicator />
              </Select.IndicatorGroup>
            </Select.Control>
            <Portal>
              <Select.Positioner>
                <Select.Content>
                  {fuelTypeCollection.items.map((item) => (
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

        <Button size="sm" onClick={handleCount} disabled={loading}>
          Посчитать
        </Button>
      </Flex>

      {loading && (
        <Flex align="center" gap={2} mt={3}>
          <Spinner size="sm" />
          <Text>Загрузка...</Text>
        </Flex>
      )}

      {error && (
        <Text mt={2} fontSize="sm" color="red.500">
          {error}
        </Text>
      )}

      {count !== null && !loading && !error && (
        <Text mt={3}>
          Количество объектов:{' '}
          <Text as="span" fontWeight="bold">
            {count}
          </Text>
        </Text>
      )}
    </Box>
  );
};

export default CountGreaterFuelTypeSection;

