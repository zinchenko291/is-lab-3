import { useState } from 'react';
import { Box, Button, Flex, Spinner, Text } from '@chakra-ui/react';
import { Gauge } from 'lucide-react';
import type { VehicleDto } from '../../api/models/vehicles';
import { useStores } from '../../store';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const MinEnginePowerSection = () => {
  const { vehiclesStore } = useStores();
  const [vehicle, setVehicle] = useState<VehicleDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLoad = async () => {
    try {
      setLoading(true);
      setError(null);
      const v = await vehiclesStore.loadMinEnginePower();
      setVehicle(v);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      setError(message ?? 'Не удалось получить объект');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box borderWidth="1px" borderRadius="md" p={4}>
      <Flex align="center" gap={2} mb={3}>
        <Gauge size={18} />
        <Text fontSize="lg" fontWeight="semibold">
          ТС с минимальной мощностью двигателя
        </Text>
      </Flex>

      <Button size="sm" onClick={handleLoad} disabled={loading}>
        Найти
      </Button>

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

      {vehicle && (
        <Box mt={3} borderWidth="1px" borderRadius="md" p={3}>
          <Text fontWeight="medium">ID: {vehicle.id}</Text>
          <Text>Название: {vehicle.name}</Text>
          <Text>Мощность: {vehicle.enginePower}</Text>
          <Text>Тип топлива: {vehicle.fuelType ?? '—'}</Text>
        </Box>
      )}
    </Box>
  );
};

export default MinEnginePowerSection;

