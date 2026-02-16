import { useState } from 'react';
import { Box, Button, Flex, Input, Spinner, Text } from '@chakra-ui/react';
import { RotateCcw } from 'lucide-react';
import type { VehicleDto } from '../../api/models/vehicles';
import { useStores } from '../../store';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const ResetDistanceSection = () => {
  const { vehiclesStore } = useStores();
  const [idInput, setIdInput] = useState('');
  const [result, setResult] = useState<VehicleDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleReset = async () => {
    const idNum = Number(idInput);
    if (!idInput.trim()) {
      setError('Введите ID');
      return;
    }
    if (Number.isNaN(idNum)) {
      setError('ID должен быть числом');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const v = await vehiclesStore.resetDistanceTravelled(idNum);
      setResult(v);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      setError(message ?? 'Не удалось сбросить пробег');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box borderWidth="1px" borderRadius="md" p={4}>
      <Flex align="center" gap={2} mb={3}>
        <RotateCcw size={18} />
        <Text fontSize="lg" fontWeight="semibold">
          Скрутить пробег до нуля
        </Text>
      </Flex>

      <Flex gap={4} align="flex-end" flexWrap="wrap" mb={3}>
        <Box minW="160px">
          <Text mb={1}>ID ТС</Text>
          <Input
            size="sm"
            type="number"
            value={idInput}
            onChange={(e) => {
              setIdInput(e.target.value);
              setError(null);
            }}
            placeholder="Введите ID"
          />
        </Box>

        <Button size="sm" onClick={handleReset} disabled={loading}>
          Сбросить
        </Button>
      </Flex>

      {loading && (
        <Flex align="center" gap={2} mt={2}>
          <Spinner size="sm" />
          <Text>Выполнение...</Text>
        </Flex>
      )}

      {error && (
        <Text mt={2} fontSize="sm" color="red.500">
          {error}
        </Text>
      )}

      {result && !error && (
        <Box mt={3} borderWidth="1px" borderRadius="md" p={3}>
          <Text fontWeight="medium">
            Пробег успешно сброшен для ТС #{result.id}
          </Text>
          <Text>Название: {result.name}</Text>
          <Text>Пробег сейчас: {result.distanceTravelled}</Text>
        </Box>
      )}
    </Box>
  );
};

export default ResetDistanceSection;

