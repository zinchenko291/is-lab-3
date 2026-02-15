import { useState } from 'react';
import {
  Box,
  Button,
  ButtonGroup,
  Flex,
  IconButton,
  Input,
  Pagination,
  Spinner,
  Table,
  Text,
} from '@chakra-ui/react';
import { ChevronLeft, ChevronRight, SlidersHorizontal } from 'lucide-react';
import type { VehicleDto } from '../../api/models/vehicles';
import { useStores } from '../../store';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const PAGE_SIZE = 10;

const EnginePowerRangeSection = () => {
  const { vehiclesStore } = useStores();
  const [minValue, setMinValue] = useState('');
  const [maxValue, setMaxValue] = useState('');
  const [results, setResults] = useState<VehicleDto[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPage = async (targetPage: number) => {
    const min = Number(minValue);
    const max = Number(maxValue);

    if (!minValue.trim() || !maxValue.trim()) {
      setError('Введите оба значения диапазона');
      return;
    }
    if (Number.isNaN(min) || Number.isNaN(max)) {
      setError('Мин/макс должны быть числами');
      return;
    }
    if (min > max) {
      setError('Минимум не может быть больше максимума');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const offset = targetPage - 1;
      const data = await vehiclesStore.findByEnginePowerRange(
        min,
        max,
        offset,
        PAGE_SIZE,
      );
      setResults(data.items);
      setTotal(data.total);
      setPage(targetPage);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      setError(message ?? 'Не удалось выполнить запрос');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    loadPage(1);
  };

  const pagesCount = total > 0 ? Math.ceil(total / PAGE_SIZE) : 0;

  return (
    <Box borderWidth="1px" borderRadius="md" p={4}>
      <Flex align="center" gap={2} mb={3}>
        <SlidersHorizontal size={18} />
        <Text fontSize="lg" fontWeight="semibold">
          ТС с мощностью двигателя в диапазоне
        </Text>
      </Flex>

      <Flex gap={4} align="flex-end" flexWrap="wrap" mb={3}>
        <Box minW="120px">
          <Text mb={1}>Мин</Text>
          <Input
            size="sm"
            type="number"
            value={minValue}
            onChange={(e) => {
              setMinValue(e.target.value);
              setError(null);
            }}
          />
        </Box>

        <Box minW="120px">
          <Text mb={1}>Макс</Text>
          <Input
            size="sm"
            type="number"
            value={maxValue}
            onChange={(e) => {
              setMaxValue(e.target.value);
              setError(null);
            }}
          />
        </Box>

        <Button size="sm" onClick={handleSearch} disabled={loading}>
          Найти
        </Button>
      </Flex>

      {loading && (
        <Flex align="center" gap={2} mt={2}>
          <Spinner size="sm" />
          <Text>Загрузка...</Text>
        </Flex>
      )}

      {error && (
        <Text mt={2} fontSize="sm" color="red.500">
          {error}
        </Text>
      )}

      {results.length > 0 && (
        <>
          <Table.Root size="sm" mt={3}>
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>Название</Table.ColumnHeader>
                <Table.ColumnHeader>Мощность</Table.ColumnHeader>
                <Table.ColumnHeader>Тип топлива</Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {results.map((v) => (
                <Table.Row key={v.id}>
                  <Table.Cell>{v.id}</Table.Cell>
                  <Table.Cell>{v.name}</Table.Cell>
                  <Table.Cell>{v.enginePower}</Table.Cell>
                  <Table.Cell>{v.fuelType ?? '—'}</Table.Cell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table.Root>

          {pagesCount > 1 && (
            <Flex justify="center" mt={3}>
              <Pagination.Root
                count={total}
                pageSize={PAGE_SIZE}
                page={page}
                onPageChange={(details: any) =>
                  loadPage(details.page as number)
                }
              >
                <ButtonGroup variant="ghost" size="sm">
                  <Pagination.PrevTrigger asChild>
                    <IconButton aria-label="Предыдущая страница">
                      <ChevronLeft size={16} />
                    </IconButton>
                  </Pagination.PrevTrigger>

                  <Pagination.Items
                    render={(p) => (
                      <IconButton
                        key={p.value}
                        variant={
                          {
                            base: 'ghost',
                            _selected: 'outline',
                          } as any
                        }
                      >
                        {p.value}
                      </IconButton>
                    )}
                  />

                  <Pagination.NextTrigger asChild>
                    <IconButton aria-label="Следующая страница">
                      <ChevronRight size={16} />
                    </IconButton>
                  </Pagination.NextTrigger>
                </ButtonGroup>
              </Pagination.Root>
            </Flex>
          )}
        </>
      )}
    </Box>
  );
};

export default EnginePowerRangeSection;

